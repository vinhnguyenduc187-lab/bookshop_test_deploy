package vn.edu.iuh.fit.bookshop_be.controllers;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookshop_be.models.Employee;
import vn.edu.iuh.fit.bookshop_be.models.Role;
import vn.edu.iuh.fit.bookshop_be.services.CustomerService;
import vn.edu.iuh.fit.bookshop_be.services.EmployeeService;
import vn.edu.iuh.fit.bookshop_be.services.OrderService;
import vn.edu.iuh.fit.bookshop_be.services.ProductService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistic")
public class StatisticalController {
    private final OrderService orderService;
    private final ProductService productService;
    private final EmployeeService employeeService;
    private final CustomerService customerService;

    public StatisticalController(OrderService orderService, ProductService productService, EmployeeService employeeService, CustomerService customerService) {
        this.orderService = orderService;
        this.productService = productService;
        this.employeeService = employeeService;
        this.customerService = customerService;
    }

    /**
     * Lấy tổng doanh thu, tổng đơn hàng, tổng sản phẩm, tổng nhân viên, tổng khách hàng trong khung thời gian
     * @param authHeader Header xác thực
     * @param startDate ngày bắt đầu (yyyy-MM-dd)
     * @param endDate ngày kết thúc (yyyy-MM-dd)
     * @return Thông tin tổng hợp
     */
    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> getTotalRevenue(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền
            if (employee.getRole() == null ||
                    (employee.getRole() != Role.MANAGER && employee.getRole() != Role.STAFF)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Nếu không truyền ngày thì mặc định lấy toàn bộ
            if (startDate == null) startDate = LocalDate.of(2000, 1, 1);
            if (endDate == null) endDate = LocalDate.now();

            Long totalOrders = orderService.countOrdersBetween(startDate, endDate);
            Double totalRevenue = orderService.calculateTotalRevenueBetween(startDate, endDate);
            Long totalProducts = productService.countProducts(); // thường không phụ thuộc thời gian
            Long totalEmployees = employeeService.countEmployees();
            Long totalCustomers = customerService.countCustomersBetween(startDate, endDate);

            Map<String, Object> data = new HashMap<>();
            data.put("totalOrders", totalOrders);
            data.put("totalRevenue", totalRevenue);
            data.put("totalProducts", totalProducts);
            data.put("totalEmployees", totalEmployees);
            data.put("totalCustomers", totalCustomers);

            response.put("data", data);
            response.put("status", "success");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Đã xảy ra lỗi khi lấy dữ liệu tổng hợp: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy tổng số sản phẩm đã bán được cho mỗi sản phẩm trong khung thời gian
     * @param authHeader Header xác thực
     * @param startDate ngày bắt đầu (yyyy-MM-dd)
     * @param endDate ngày kết thúc (yyyy-MM-dd)
     * @return Danh sách sản phẩm và tổng số lượng bán được
     */
    @GetMapping("/products-sold")
    public ResponseEntity<Map<String, Object>> getTotalProductsSold(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền truy cập
            if (employee.getRole() == null ||
                    (employee.getRole() != Role.MANAGER && employee.getRole() != Role.STAFF)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Nếu không có mốc thời gian -> lấy toàn bộ dữ liệu
            if (startDate == null) startDate = LocalDate.of(2000, 1, 1);
            if (endDate == null) endDate = LocalDate.now();

            Map<String, Object> data = new HashMap<>();
            for (var product : productService.getAllProducts()) {
                Long totalSold = orderService.countTotalProductSoldBetween(product.getId(), startDate, endDate);
                data.put(product.getProductName(), totalSold);
            }

            response.put("status", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Đã xảy ra lỗi khi lấy số lượng sản phẩm đã bán: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



}
