package vn.edu.iuh.fit.bookshop_be.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookshop_be.dtos.StockReceiptRequest;
import vn.edu.iuh.fit.bookshop_be.models.*;
import vn.edu.iuh.fit.bookshop_be.services.EmployeeService;
import vn.edu.iuh.fit.bookshop_be.services.StockReceiptService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-receipt")
public class StockReceiptController {
    private final StockReceiptService stockReceiptService;
    private final EmployeeService employeeService;

    public StockReceiptController(StockReceiptService stockReceiptService, EmployeeService employeeService) {
        this.stockReceiptService = stockReceiptService;
        this.employeeService = employeeService;
    }

    @PostMapping("/createReceipt")
    public ResponseEntity<Map<String, Object>> createReceipt(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody StockReceiptRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);

            if (employee == null ) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            if(employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Thực hiện tạo phiếu nhập kho
            Map<String, Object> data = new HashMap<>();
            StockReceipt stockReceipt = stockReceiptService.save(request.getNameStockReceipt() ,request.getTypeStockReceipt(), employee, request.getNote(), request.getProducts());
            response.put("status", "success");
            response.put("message", "Đặt hàng thành công");

            data.put("stockReceiptId", stockReceipt.getId());
            data.put("typeStockReceipt", stockReceipt.getTypeStockReceipt());
            data.put("createdAt", stockReceipt.getCreatedAt());
            data.put("note", stockReceipt.getNote());
            data.put("employee", stockReceipt.getEmployee());
            data.put("details", stockReceipt.getDetails());

            response.put("data", data);


            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi đặt hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllStockReceipts(
            @RequestHeader("Authorization") String authHeader
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("stockReceipts", stockReceiptService.getAllStockReceipts());
            response.put("status", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách phiếu nhập xuất kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getDateBetween")
    public ResponseEntity<Map<String, Object>> getStockReceiptsDateBetween(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            List<StockReceipt> receipts = stockReceiptService.getStockReceiptsDateBetween(startDate, endDate);
            Map<String, Object> data = new HashMap<>();
            data.put("stockReceipts", receipts);

            response.put("status", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách phiếu nhập xuất kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStockReceiptById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            StockReceipt stockReceipt = stockReceiptService.getStockReceiptById(id);
            if (stockReceipt == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy phiếu nhập xuất kho với ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("stockReceipt", stockReceipt);
            response.put("status", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy phiếu nhập xuất kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
