package co.edu.uniremington;

import co.edu.uniremington.model.Product;
import co.edu.uniremington.model.Sale;
import co.edu.uniremington.model.SaleItem;
import co.edu.uniremington.service.ProductService;
import co.edu.uniremington.service.SaleService;
import co.edu.uniremington.service.impl.ProductServiceImpl;
import co.edu.uniremington.service.impl.SaleServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StoreApplication {
    private final Scanner scanner;
    private final ProductService productService;
    private final SaleService saleService;

    public StoreApplication() {
        this.scanner = new Scanner(System.in);
        this.productService = new ProductServiceImpl();
        this.saleService = new SaleServiceImpl(productService);
    }

    public void start() {
        while (true) {
            showMenu();
            int option = readOption();
            if (option == 0) break;
            processOption(option);
        }
    }

    private void showMenu() {
        System.out.println("\n=== Tienda Funcional ===");
        System.out.println("1. Registrar venta");
        System.out.println("2. Ver todas las ventas");
        System.out.println("3. Agregar producto");
        System.out.println("4. Actualizar producto");
        System.out.println("5. Ver todos los productos");
        System.out.println("6. Buscar producto por ID");
        System.out.println("7. Ver productos sin stock");
        System.out.println("8. Ver promedios de ventas");
        System.out.println("9. Ver producto más vendido");
        System.out.println("10. Ver producto menos vendido");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private int readOption() {
        return Integer.parseInt(scanner.nextLine());
    }

    private void processOption(int option) {
        switch (option) {
            case 1 -> registerSale();
            case 2 -> showAllSales();
            case 3 -> addProduct();
            case 4 -> updateProduct();
            case 5 -> showAllProducts();
            case 6 -> findProductById();
            case 7 -> showOutOfStockProducts();
            case 8 -> showSalesAverages();
            case 9 -> showMostSoldProduct();
            case 10 -> showLeastSoldProduct();
            default -> System.out.println("Opción inválida");
        }
    }

    private void registerSale() {
        List<SaleItem> items = new ArrayList<>();
        while (true) {
            System.out.print("ID del producto (o 'fin' para terminar): ");
            String productId = scanner.nextLine();
            if (productId.equalsIgnoreCase("fin")) break;

            var product = productService.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            System.out.print("Cantidad: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            if (quantity > product.getStock()) {
                System.out.println("Stock insuficiente");
                continue;
            }

            items.add(SaleItem.builder()
                    .product(product)
                    .quantity(quantity)
                    .build());
        }

        if (!items.isEmpty()) {
            Sale sale = Sale.builder().items(items).build();
            sale = saleService.registerSale(sale);
            System.out.println("Venta registrada con código: " + sale.getCode());
            System.out.println("Total: $" + sale.getTotal());
        }
    }

    private void showAllSales() {
        saleService.findAll().forEach(sale -> {
            System.out.println("\nCódigo: " + sale.getCode());
            System.out.println("Fecha: " + sale.getDate());
            System.out.println("Total: $" + sale.getTotal());
            System.out.println("Items:");
            sale.getItems().forEach(item -> 
                System.out.println("- " + item.getProduct().getName() + 
                    " x " + item.getQuantity() + 
                    " = $" + item.getSubtotal())
            );
        });
    }

    private void addProduct() {
        System.out.print("Nombre: ");
        String name = scanner.nextLine();
        System.out.print("Descripción: ");
        String description = scanner.nextLine();
        System.out.print("Precio: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Stock: ");
        int stock = Integer.parseInt(scanner.nextLine());

        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .build();

        product = productService.addProduct(product);
        System.out.println("Producto agregado con ID: " + product.getId());
    }

    private void updateProduct() {
        System.out.print("ID del producto: ");
        String id = scanner.nextLine();

        productService.findById(id).ifPresentOrElse(
            product -> {
                System.out.print("Nuevo nombre (" + product.getName() + "): ");
                String name = scanner.nextLine();
                System.out.print("Nueva descripción (" + product.getDescription() + "): ");
                String description = scanner.nextLine();
                System.out.print("Nuevo precio (" + product.getPrice() + "): ");
                String priceStr = scanner.nextLine();
                System.out.print("Nuevo stock (" + product.getStock() + "): ");
                String stockStr = scanner.nextLine();

                Product updatedProduct = Product.builder()
                        .id(id)
                        .name(name.isEmpty() ? product.getName() : name)
                        .description(description.isEmpty() ? product.getDescription() : description)
                        .price(priceStr.isEmpty() ? product.getPrice() : Double.parseDouble(priceStr))
                        .stock(stockStr.isEmpty() ? product.getStock() : Integer.parseInt(stockStr))
                        .build();

                productService.updateProduct(updatedProduct);
                System.out.println("Producto actualizado exitosamente");
            },
            () -> System.out.println("Producto no encontrado")
        );
    }

    private void showAllProducts() {
        productService.findAll().forEach(this::printProduct);
    }

    private void findProductById() {
        System.out.print("ID del producto: ");
        String id = scanner.nextLine();
        productService.findById(id)
                .ifPresentOrElse(
                    this::printProduct,
                    () -> System.out.println("Producto no encontrado")
                );
    }

    private void showOutOfStockProducts() {
        var products = productService.findOutOfStock();
        if (products.isEmpty()) {
            System.out.println("No hay productos sin stock");
        } else {
            System.out.println("\nProductos sin stock:");
            products.forEach(this::printProduct);
        }
    }

    private void showSalesAverages() {
        System.out.println("\nPromedios de ventas:");
        System.out.printf("Semanal: $%.2f%n", saleService.calculateWeeklyAverage());
        System.out.printf("Mensual: $%.2f%n", saleService.calculateMonthlyAverage());
        System.out.printf("Anual: $%.2f%n", saleService.calculateYearlyAverage());
    }

    private void showMostSoldProduct() {
        productService.getMostSoldProduct()
                .ifPresentOrElse(
                    product -> {
                        System.out.println("\nProducto más vendido:");
                        printProduct(product);
                    },
                    () -> System.out.println("No hay ventas registradas")
                );
    }

    private void showLeastSoldProduct() {
        productService.getLeastSoldProduct()
                .ifPresentOrElse(
                    product -> {
                        System.out.println("\nProducto menos vendido:");
                        printProduct(product);
                    },
                    () -> System.out.println("No hay ventas registradas")
                );
    }

    private void printProduct(Product product) {
        System.out.println("\nID: " + product.getId());
        System.out.println("Nombre: " + product.getName());
        System.out.println("Descripción: " + product.getDescription());
        System.out.println("Precio: $" + product.getPrice());
        System.out.println("Stock: " + product.getStock());
    }

    public static void main(String[] args) {
        new StoreApplication().start();
    }
}
