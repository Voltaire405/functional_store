package co.edu.uniremington.service;

import co.edu.uniremington.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface ProductService {
    Product addProduct(Product product);
    Optional<Product> updateProduct(Product product);
    Optional<Product> findById(String id);
    List<Product> findAll();
    List<Product> findByPredicate(Predicate<Product> predicate);
    List<Product> findOutOfStock();
    Optional<Product> getMostSoldProduct();
    Optional<Product> getLeastSoldProduct();
}
