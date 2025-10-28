package vn.edu.iuh.fit.bookshop_be.dtos;

import jakarta.persistence.Column;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PromotionRequest {
    private String name;
    private String code;
    private String description;
    private Double discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
}
