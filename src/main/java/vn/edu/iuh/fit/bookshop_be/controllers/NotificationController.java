package vn.edu.iuh.fit.bookshop_be.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookshop_be.dtos.NotificationRequest;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.models.Employee;
import vn.edu.iuh.fit.bookshop_be.models.Notification;
import vn.edu.iuh.fit.bookshop_be.models.Role;
import vn.edu.iuh.fit.bookshop_be.services.CustomerService;
import vn.edu.iuh.fit.bookshop_be.services.EmployeeService;
import vn.edu.iuh.fit.bookshop_be.services.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    private final NotificationService notificationService;
    private final EmployeeService employeeService;
    private final CustomerService customerService;

    public NotificationController(NotificationService notificationService, EmployeeService employeeService, CustomerService customerService) {
        this.notificationService = notificationService;
        this.employeeService = employeeService;
        this.customerService = customerService;
    }

    /**
     * Tạo Thông báo
     * @param authHeader Authorization
     * @param request Thông tin Thông báo
     * @return ResponseEntity với trạng thái và dữ liệu
     */
    @PostMapping("/createNotification")
    public ResponseEntity<Map<String, Object>> createPromotion(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody NotificationRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để tạo thông báo");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền của người dùng
            if (employee.getRole() == null || (employee.getRole() != Role.MANAGER && employee.getRole() != Role.STAFF)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền tạo thông báo");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            String title = request.getTitle();
            String message = request.getMessage();

            // Kiểm tra thông tin
            if (title.isEmpty() || message.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Thông tin không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Tạo Thông báo
            Notification notification = notificationService.createNotification(title, message);


            response.put("status", "success");
            response.put("message", "Thêm thông báo thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("notification", notification);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi tạo thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllNotifications() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Notification> notifications = notificationService.getAllNotifications();

            response.put("status", "success");
            response.put("message", "Lấy danh sách thông báo thành công");
            response.put("data", notifications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/countUnread")
    public ResponseEntity<Map<String, Object>> countUnreadNotificationsForCustomer( @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {

            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để đếm thông báo chưa đọc");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Integer count = notificationService.countUnreadNotificationsForCustomer(customer.getId());

            response.put("status", "success");
            response.put("message", "Đếm thông báo chưa đọc thành công");
            response.put("data", count);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi đếm thông báo chưa đọc: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/readAll")
    public ResponseEntity<Map<String, Object>> readAllNotificationsForCustomer( @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để dánh dấu tất cả thông báo đã đọc");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            notificationService.readNotification(customer.getId());

            response.put("status", "success");
            response.put("message", "Đánh dấu tất cả thông báo đã đọc thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi đánh dấu tất cả thông báo đã đọc: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/updateNotification/{id}")
    public ResponseEntity<Map<String, Object>> updateNotification(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id,
            @RequestBody NotificationRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để cập nhật thông báo");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền của người dùng
            if (employee.getRole() == null || (employee.getRole() != Role.MANAGER && employee.getRole() != Role.STAFF)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền cập nhật thông báo");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            String title = request.getTitle();
            String message = request.getMessage();

            // Kiểm tra thông tin
            if (title.isEmpty() || message.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Thông tin không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Notification existingNotification = notificationService.findById(id);
            if (existingNotification == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy thông báo với id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            existingNotification.setTitle(title);
            existingNotification.setMessage(message);

            Notification updatedNotification = notificationService.updateNotification(id ,existingNotification);

            response.put("status", "success");
            response.put("message", "Cập nhật thông báo thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("notification", updatedNotification);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi cập nhật thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/deleteNotification/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xóa thông báo");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền của người dùng
            if (employee.getRole() == null || (employee.getRole() != Role.MANAGER && employee.getRole() != Role.STAFF)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền xóa thông báo");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Notification existingNotification = notificationService.findById(id);
            if (existingNotification == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy thông báo với id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            notificationService.deleteNotification(id);

            response.put("status", "success");
            response.put("message", "Xóa thông báo thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi xóa thông báo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
