package co.edu.uniremington.service.impl;

import co.edu.uniremington.model.Sale;
import co.edu.uniremington.model.SaleItem;
import co.edu.uniremington.service.ProductService;
import co.edu.uniremington.service.SaleService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class SaleServiceImpl implements SaleService {
    private final Map<String, Sale> sales;
    private final ProductService productService;

    public SaleServiceImpl(ProductService productService) {
        this.sales = new HashMap<>();
        this.productService = productService;
    }

    @Override
    public Sale registerSale(Sale sale) {
        if (sale.getId() == null) {
            sale = Sale.builder()
                    .id(UUID.randomUUID().toString())
                    .code(generateSaleCode())
                    .date(LocalDateTime.now())
                    .items(processSaleItems(sale.getItems()))
                    .total(calculateTotal(sale.getItems()))
                    .build();
        }
        updateInventory(sale.getItems());
        updateProductSales(sale.getItems());
        sales.put(sale.getId(), sale);
        return sale;
    }

    private List<SaleItem> processSaleItems(List<SaleItem> items) {
        return items.stream()
                .map(item -> SaleItem.builder()
                        .product(item.getProduct())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    private double calculateTotal(List<SaleItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    private void updateInventory(List<SaleItem> items) {
        items.forEach(item -> {
            var product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            productService.updateProduct(product);
        });
    }

    private void updateProductSales(List<SaleItem> items) {
        items.forEach(item -> 
            ((ProductServiceImpl) productService).incrementProductSales(
                item.getProduct().getId(), 
                item.getQuantity()
            )
        );
    }

    private String generateSaleCode() {
        return "SALE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public Optional<Sale> findById(String id) {
        return Optional.ofNullable(sales.get(id));
    }

    @Override
    public List<Sale> findAll() {
        return new ArrayList<>(sales.values());
    }

    @Override
    public double calculateAverageSalesByPeriod(LocalDateTime start, LocalDateTime end) {
        var salesInPeriod = sales.values().stream()
                .filter(sale -> !sale.getDate().isBefore(start) && !sale.getDate().isAfter(end))
                .mapToDouble(Sale::getTotal)
                .sum();
        
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        return salesInPeriod / days;
    }

    @Override
    public double calculateWeeklyAverage() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.minusWeeks(1);
        return calculateAverageSalesByPeriod(weekStart, now);
    }

    @Override
    public double calculateMonthlyAverage() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.minusMonths(1);
        return calculateAverageSalesByPeriod(monthStart, now);
    }

    @Override
    public double calculateYearlyAverage() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yearStart = now.minusYears(1);
        return calculateAverageSalesByPeriod(yearStart, now);
    }
}
