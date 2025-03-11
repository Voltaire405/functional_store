package co.edu.uniremington.service.impl;

import co.edu.uniremington.model.Product;
import co.edu.uniremington.service.ProductService;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ProductServiceImpl implements ProductService {
    private final Map<String, Product> products;
    private final Map<String, Long> productSales;

    public ProductServiceImpl() {
        this.products = new HashMap<>();
        this.productSales = new HashMap<>();
    }

    @Override
    public Product addProduct(Product product) {
        if (product.getId() == null) {
            product = Product.builder()
                    .id(UUID.randomUUID().toString())
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .stock(product.getStock())
                    .build();
        }
        products.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> updateProduct(Product product) {
        return Optional.ofNullable(products.get(product.getId()))
                .map(existing -> {
                    products.put(product.getId(), product);
                    return product;
                });
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public List<Product> findByPredicate(Predicate<Product> predicate) {
        return products.values().stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findOutOfStock() {
        return findByPredicate(product -> product.getStock() == 0);
    }

    @Override
    public Optional<Product> getMostSoldProduct() {
        return productSales.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> products.get(entry.getKey()));
    }

    @Override
    public Optional<Product> getLeastSoldProduct() {
        return productSales.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(entry -> products.get(entry.getKey()));
    }

    public void incrementProductSales(String productId, long quantity) {
        productSales.merge(productId, quantity, Long::sum);
    }
}
