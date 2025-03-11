package co.edu.uniremington.service;

import co.edu.uniremington.model.Product;
import co.edu.uniremington.model.Sale;
import co.edu.uniremington.model.SaleItem;
import co.edu.uniremington.service.impl.ProductServiceImpl;
import co.edu.uniremington.service.impl.SaleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StoreServiceTest {
    private ProductService productService;
    private SaleService saleService;
    private Product laptop;
    private Product phone;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl();
        saleService = new SaleServiceImpl(productService);

        // Create test products
        laptop = productService.addProduct(Product.builder()
                .name("Laptop")
                .description("High-end laptop")
                .price(1200.0)
                .stock(10)
                .build());

        phone = productService.addProduct(Product.builder()
                .name("Phone")
                .description("Smartphone")
                .price(800.0)
                .stock(15)
                .build());
    }

    @Test
    void shouldAddAndFindProduct() {
        var product = productService.addProduct(Product.builder()
                .name("Tablet")
                .description("Android tablet")
                .price(300.0)
                .stock(5)
                .build());

        assertThat(product.getId()).isNotNull();
        var found = productService.findById(product.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Tablet");
    }

    @Test
    void shouldUpdateProduct() {
        laptop.setPrice(1300.0);
        var updated = productService.updateProduct(laptop);
        assertThat(updated).isPresent();
        assertThat(updated.get().getPrice()).isEqualTo(1300.0);
    }

    @Test
    void shouldRegisterSale() {
        var saleItems = List.of(
            SaleItem.builder()
                .product(laptop)
                .quantity(1)
                .build(),
            SaleItem.builder()
                .product(phone)
                .quantity(2)
                .build()
        );

        var sale = Sale.builder()
                .items(saleItems)
                .build();

        var registeredSale = saleService.registerSale(sale);

        assertThat(registeredSale.getId()).isNotNull();
        assertThat(registeredSale.getCode()).isNotNull();
        assertThat(registeredSale.getTotal()).isEqualTo(2800.0); // 1200 + (800 * 2)
        
        // Verify inventory was updated
        var updatedLaptop = productService.findById(laptop.getId()).get();
        var updatedPhone = productService.findById(phone.getId()).get();
        assertThat(updatedLaptop.getStock()).isEqualTo(9);
        assertThat(updatedPhone.getStock()).isEqualTo(13);
    }

    @Test
    void shouldFindOutOfStockProducts() {
        var noStock = productService.addProduct(Product.builder()
                .name("Out of stock")
                .description("No stock product")
                .price(100.0)
                .stock(0)
                .build());

        var outOfStock = productService.findOutOfStock();
        assertThat(outOfStock).hasSize(1);
        assertThat(outOfStock.get(0).getId()).isEqualTo(noStock.getId());
    }

    @Test
    void shouldIdentifyMostAndLeastSoldProducts() {
        // Register multiple sales
        for (int i = 0; i < 3; i++) {
            var saleItems = List.of(
                SaleItem.builder()
                    .product(laptop)
                    .quantity(2)
                    .build()
            );
            saleService.registerSale(Sale.builder().items(saleItems).build());
        }

        var saleItems = List.of(
            SaleItem.builder()
                .product(phone)
                .quantity(1)
                .build()
        );
        saleService.registerSale(Sale.builder().items(saleItems).build());

        var mostSold = productService.getMostSoldProduct();
        var leastSold = productService.getLeastSoldProduct();

        assertThat(mostSold).isPresent();
        assertThat(leastSold).isPresent();
        assertThat(mostSold.get().getId()).isEqualTo(laptop.getId());
        assertThat(leastSold.get().getId()).isEqualTo(phone.getId());
    }

    @Test
    void shouldCalculateSalesAverages() {
        // Register a sale
        var saleItems = List.of(
            SaleItem.builder()
                .product(laptop)
                .quantity(1)
                .build(),
            SaleItem.builder()
                .product(phone)
                .quantity(1)
                .build()
        );

        saleService.registerSale(Sale.builder().items(saleItems).build());

        assertThat(saleService.calculateWeeklyAverage()).isPositive();
        assertThat(saleService.calculateMonthlyAverage()).isPositive();
        assertThat(saleService.calculateYearlyAverage()).isPositive();
    }
}
