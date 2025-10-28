package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;

@Data
public class ProductStockReceiptRequest {
    private Integer productId;
    private Integer quantity;
    private String note;
    private String supplier;
}
