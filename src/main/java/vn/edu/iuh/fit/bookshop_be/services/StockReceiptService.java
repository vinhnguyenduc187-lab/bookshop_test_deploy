package vn.edu.iuh.fit.bookshop_be.services;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.dtos.ProductOrderRequest;
import vn.edu.iuh.fit.bookshop_be.dtos.ProductStockReceiptRequest;
import vn.edu.iuh.fit.bookshop_be.models.*;
import vn.edu.iuh.fit.bookshop_be.repositories.InventoryRepository;
import vn.edu.iuh.fit.bookshop_be.repositories.StockReceiptDetailRepository;
import vn.edu.iuh.fit.bookshop_be.repositories.StockReceiptRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockReceiptService {
    private final StockReceiptRepository stockReceiptRepository;
    private final StockReceiptDetailRepository stockReceiptDetailRepository;
    private final ProductService productService;
    private final InventoryRepository inventoryRepository;

    public StockReceiptService(StockReceiptRepository stockReceiptRepository, StockReceiptDetailRepository stockReceiptDetailRepository, ProductService productService, InventoryRepository inventoryRepository) {
        this.stockReceiptRepository = stockReceiptRepository;
        this.stockReceiptDetailRepository = stockReceiptDetailRepository;
        this.productService = productService;
        this.inventoryRepository = inventoryRepository;
    }

    public StockReceipt save(String nameStockReceipt , TypeStockReceipt typeStockReceipt, Employee employee, String note , List<ProductStockReceiptRequest> productStockReceiptRequests) {
        StockReceipt stockReceipt = new StockReceipt();
        stockReceipt.setTypeStockReceipt(typeStockReceipt);
        if(employee == null){
            stockReceipt.setEmployee(null);
        }
        else{
            stockReceipt.setEmployee(employee);
        }
        stockReceipt.setNote(note);
        stockReceipt.setNameStockReceipt(nameStockReceipt);
        List<StockReceiptDetail> details = new ArrayList<>();
        if(typeStockReceipt == TypeStockReceipt.IMPORT) {
            for(ProductStockReceiptRequest request : productStockReceiptRequests) {
                StockReceiptDetail detail = new StockReceiptDetail();
                detail.setStockReceipt(stockReceipt);
                Product product = productService.findById(request.getProductId());
                detail.setProduct(product);
                detail.setQuantity(request.getQuantity());
                detail.setNote(request.getNote());
                detail.setSupplier(request.getSupplier());
                details.add(detail);
                // Cập nhật tồn kho
                Inventory inventory = inventoryRepository.findByProduct(product).orElse(null);
                if (inventory != null) {
                    inventory.setActualQuantity(inventory.getActualQuantity() + request.getQuantity());
                    inventory.recalculateAvailable();
                    inventoryRepository.save(inventory);
                }
                productService.save(product);
            }
        } else if(typeStockReceipt == TypeStockReceipt.EXPORT) {
            for(ProductStockReceiptRequest request : productStockReceiptRequests) {
                StockReceiptDetail detail = new StockReceiptDetail();
                detail.setStockReceipt(stockReceipt);
                Product product = productService.findById(request.getProductId());
                detail.setProduct(product);
                detail.setQuantity(request.getQuantity());
                detail.setNote(request.getNote());
                detail.setSupplier(request.getSupplier());
                details.add(detail);
                // Cập nhật tồn kho
                Inventory inventory = inventoryRepository.findByProduct(product).orElse(null);
                if (inventory != null) {
                    inventory.setActualQuantity(inventory.getActualQuantity() - request.getQuantity());
                    inventory.recalculateAvailable();
                    inventoryRepository.save(inventory);
                }
                productService.save(product);
            }
        }
        stockReceipt.setDetails(details);
        stockReceipt = stockReceiptRepository.save(stockReceipt);
        for(StockReceiptDetail detail : details) {
            stockReceiptDetailRepository.save(detail);
        }
        return stockReceipt;
    }

    public List<StockReceipt> getAllStockReceipts() {
        return stockReceiptRepository.findAll();
    }

    public List<StockReceipt> getStockReceiptsDateBetween(LocalDate startDate, LocalDate endDate) {
        // Nếu người dùng không truyền ngày → mặc định lấy tháng hiện tại
        LocalDateTime startDateTime = (startDate != null)
                ? startDate.atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();

        LocalDateTime endDateTime = (endDate != null)
                ? endDate.atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        return stockReceiptRepository.getStockReceiptsDateBetween(startDateTime, endDateTime);
    }

    public StockReceipt getStockReceiptById(Integer id) {
        return stockReceiptRepository.findById(id).orElse(null);
    }


}
