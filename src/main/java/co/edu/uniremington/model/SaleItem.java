package co.edu.uniremington.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaleItem {
    private Product product;
    private int quantity;
    private double subtotal;
}
