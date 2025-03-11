package co.edu.uniremington.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private int stock;
}
