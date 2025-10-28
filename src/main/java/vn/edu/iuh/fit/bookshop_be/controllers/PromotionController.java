package vn.edu.iuh.fit.bookshop_be.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookshop_be.dtos.PromotionRequest;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.models.Employee;
import vn.edu.iuh.fit.bookshop_be.models.Promotion;
import vn.edu.iuh.fit.bookshop_be.models.Role;
import vn.edu.iuh.fit.bookshop_be.services.EmployeeService;
import vn.edu.iuh.fit.bookshop_be.services.PromotionService;
import vn.edu.iuh.fit.bookshop_be.services.CustomerService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotion")
public class PromotionController {
    private final PromotionService promotionService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;

    public PromotionController(PromotionService promotionService, CustomerService customerService, EmployeeService employeeService) {
        this.promotionService = promotionService;
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Tạo khuyến mãi mới
     * @param authHeader
     * @param request
     * @return ResponseEntity với thông tin khuyến mãi đã tạo
     * @throws Exception nếu có lỗi xảy ra trong quá trình tạo khuyến mãi
     */
    @PostMapping("/createPromotion")
    public ResponseEntity<Map<String, Object>> createPromotion(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PromotionRequest request
    ) {
        Promotion promotion = new Promotion();
        promotion.setName(request.getName());
        promotion.setCode(request.getCode());
        promotion.setDescription(request.getDescription());
        promotion.setDiscountPercent(request.getDiscountPercent());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để tạo khuyến mãi");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền của người dùng
            if (employee.getRole() == null || employee.getRole() != Role.MANAGER) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền tạo khuyến mãi");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Kiểm tra thông tin khuyến mãi
            if (promotion.getCode() == null || promotion.getCode().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Mã khuyến mãi không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (promotion.getDiscountPercent() == null || promotion.getDiscountPercent() <= 0) {
                response.put("status", "error");
                response.put("message", "Phần trăm giảm giá phải lớn hơn 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (promotion.getStartDate() == null || promotion.getEndDate() == null) {
                response.put("status", "error");
                response.put("message", "Ngày bắt đầu và kết thúc không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
                response.put("status", "error");
                response.put("message", "Ngày bắt đầu phải trước ngày kết thúc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if(promotion.getEndDate().isBefore(java.time.LocalDate.now())) {
                response.put("status", "error");
                response.put("message", "Ngày kết thúc phải sau ngày hiện tại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Tạo khuyến mãi
            if(promotion.getStartDate().isBefore(java.time.LocalDate.now()) && promotion.getEndDate().isAfter(java.time.LocalDate.now())) {
                promotion.setStatus(vn.edu.iuh.fit.bookshop_be.models.PromotionStatus.ACTIVE);
            } else if (promotion.getStartDate().isBefore(java.time.LocalDate.now()) && promotion.getEndDate().isBefore(java.time.LocalDate.now())) {
                promotion.setStatus(vn.edu.iuh.fit.bookshop_be.models.PromotionStatus.EXPIRED);
            } else {
                promotion.setStatus(vn.edu.iuh.fit.bookshop_be.models.PromotionStatus.INACTIVE);
            }
            Promotion createdPromotion = promotionService.createPromotion(promotion);

            response.put("status", "success");
            response.put("message", "Thêm khuyến mãi thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("promotion", createdPromotion);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi tạo khuyến mãi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách tất cả khuyến mãi
     * @param authHeader
     * @return ResponseEntity với danh sách khuyến mãi
     */
    @GetMapping("/getAllPromotions")
    public ResponseEntity<Map<String, Object>> getAllPromotions(
            @RequestHeader("Authorization") String authHeader
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để lấy danh sách tất cả khuyến mãi");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền của người dùng
            if (employee.getRole() == null || employee.getRole() != Role.MANAGER) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền lấy danh sách tất cả khuyến mãi");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Lấy danh sách khuyến mãi
            List<Promotion> promotions = promotionService.getAllPromotions();


            response.put("status", "success");
            response.put("message", "Lấy danh sách tất cả khuyến mãi thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("promotions", promotions);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách tất cả khuyến mãi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin khuyến mãi theo ID
     * @param authHeader
     * @param id ID của khuyến mãi cần lấy thông tin
     * @return ResponseEntity với thông tin khuyến mãi
     */
    @GetMapping("/getPromotionById/{id}")
    public ResponseEntity<Map<String, Object>> getPromotionById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để lấy thông tin khuyến mãi");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền của người dùng
            if (employee.getRole() == null || employee.getRole() != Role.MANAGER) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền lấy thông tin khuyến mãi");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Promotion promotion = promotionService.getPromotionById(id);
            if (promotion == null) {
                response.put("status", "error");
                response.put("message", "Khuyến mãi không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("status", "success");
            response.put("message", "Lấy thông tin khuyến mãi thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("promotion", promotion);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy thông tin khuyến mãi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật thông tin khuyến mãi
     * @param authHeader
     * @param id ID của khuyến mãi cần cập nhật
     * @param request Thông tin khuyến mãi mới
     * @return ResponseEntity với thông tin khuyến mãi đã cập nhật
     */
    @PutMapping("/updatePromotion/{id}")
    public ResponseEntity<Map<String, Object>> updatePromotion(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id,
            @RequestBody PromotionRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để cập nhật khuyến mãi");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền của người dùng
            if (employee.getRole() == null || employee.getRole() != Role.MANAGER) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền cập nhật khuyến mãi");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Promotion promotion = promotionService.getPromotionById(id);
            if (promotion == null) {
                response.put("status", "error");
                response.put("message", "Khuyến mãi không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            promotion.setName(request.getName());
            promotion.setCode(request.getCode());
            promotion.setDescription(request.getDescription());
            promotion.setDiscountPercent(request.getDiscountPercent());
            promotion.setStartDate(request.getStartDate());
            promotion.setEndDate(request.getEndDate());

            // Kiểm tra thông tin khuyến mãi
            if (promotion.getCode() == null || promotion.getCode().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Mã khuyến mãi không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (promotion.getDiscountPercent() == null || promotion.getDiscountPercent() <= 0) {
                response.put("status", "error");
                response.put("message", "Phần trăm giảm giá phải lớn hơn 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (promotion.getStartDate() == null || promotion.getEndDate() == null) {
                response.put("status", "error");
                response.put("message", "Ngày bắt đầu và kết thúc không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (promotion.getStartDate().isAfter(promotion.getEndDate())) {
                response.put("status", "error");
                response.put("message", "Ngày bắt đầu phải trước ngày kết thúc");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if(promotion.getEndDate().isBefore(java.time.LocalDate.now())) {
                response.put("status", "error");
                response.put("message", "Ngày kết thúc phải sau ngày hiện tại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Tạo khuyến mãi
            if(promotion.getStartDate().isBefore(java.time.LocalDate.now()) && promotion.getEndDate().isAfter(java.time.LocalDate.now())) {
                promotion.setStatus(vn.edu.iuh.fit.bookshop_be.models.PromotionStatus.ACTIVE);
            } else if (promotion.getStartDate().isBefore(java.time.LocalDate.now()) && promotion.getEndDate().isBefore(java.time.LocalDate.now())) {
                promotion.setStatus(vn.edu.iuh.fit.bookshop_be.models.PromotionStatus.EXPIRED);
            } else {
                promotion.setStatus(vn.edu.iuh.fit.bookshop_be.models.PromotionStatus.INACTIVE);
            }

            // Cập nhật khuyến mãi
            Promotion updatedPromotion = promotionService.updatePromotion(id, promotion);

            response.put("status", "success");
            response.put("message", "Cập nhật khuyến mãi thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("promotion", updatedPromotion);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi cập nhật khuyến mãi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa khuyến mãi
     * @param authHeader
     * @param id ID của khuyến mãi cần xóa
     * @return ResponseEntity với thông báo kết quả xóa
     */
    @DeleteMapping("/deletePromotion/{id}")
    public ResponseEntity<Map<String, Object>> deletePromotion(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xóa khuyến mãi");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Kiểm tra quyền của người dùng
            if (employee.getRole() == null || employee.getRole() != Role.MANAGER) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền xóa khuyến mãi");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            promotionService.deletePromotion(id);

            response.put("status", "success");
            response.put("message", "Xóa khuyến mãi thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi xóa khuyến mãi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin khuyến mãi theo mã code
     * @param authHeader
     * @param code Mã code của khuyến mãi cần lấy thông tin
     * @return ResponseEntity với thông tin khuyến mãi
     */
    @GetMapping("/getPromotionByCode/{code}")
    public ResponseEntity<Map<String, Object>> getPromotionByCode(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String code
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (employee == null && customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để lấy thông tin khuyến mãi");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            Promotion promotion = promotionService.findByCode(code);
            if (promotion == null) {
                response.put("status", "error");
                response.put("message", "Khuyến mãi không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("status", "success");
            response.put("message", "Lấy thông tin khuyến mãi thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("promotion", promotion);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy thông tin khuyến mãi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
