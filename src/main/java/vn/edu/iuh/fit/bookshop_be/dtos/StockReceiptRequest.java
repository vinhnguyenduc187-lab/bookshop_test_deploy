package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;
import vn.edu.iuh.fit.bookshop_be.models.TypeStockReceipt;

import java.util.List;

@Data
public class StockReceiptRequest {
    private List<ProductStockReceiptRequest> products;
    private String nameStockReceipt;
    private String note;
    private TypeStockReceipt typeStockReceipt;
}
