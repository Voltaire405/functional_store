package co.edu.uniremington.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Sale {
    private String id;
    private String code;
    private LocalDateTime date;
    private List<SaleItem> items;
    private double total;
}
