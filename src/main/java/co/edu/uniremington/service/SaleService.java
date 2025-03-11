package co.edu.uniremington.service;

import co.edu.uniremington.model.Sale;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleService {
    Sale registerSale(Sale sale);
    Optional<Sale> findById(String id);
    List<Sale> findAll();
    double calculateAverageSalesByPeriod(LocalDateTime start, LocalDateTime end);
    double calculateWeeklyAverage();
    double calculateMonthlyAverage();
    double calculateYearlyAverage();
}
