package vn.edu.iuh.fit.bookshop_be.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import vn.edu.iuh.fit.bookshop_be.dtos.PlaceOrderRequest;
import vn.edu.iuh.fit.bookshop_be.models.*;
import vn.edu.iuh.fit.bookshop_be.services.*;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/order")
public class OrderController{
    private final OrderService orderService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final AddressService addressService;
    private final VNPayService vNPayService;
    private final ProductService productService;

    public OrderController(OrderService orderService, CustomerService customerService, EmployeeService employeeService, AddressService addressService, VNPayService vNPayService, ProductService productService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.addressService = addressService;
        this.vNPayService = vNPayService;
        this.productService = productService;
    }

    @Value("${base_url_FE}")
    private String baseUrlFE;

    /**
     * Đặt hàng
     * @param authHeader
     * @param request
     * @return trả về thông tin đơn hàng đã đặt
     */
    @PostMapping("/placeOrder")
    public ResponseEntity<Map<String, Object>> updateAvatar(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody PlaceOrderRequest request
            ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);

            if (customer == null ) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để cập nhật ảnh đại diện");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            PaymentMethod paymentMethod = request.getPaymentMethod();
            if (paymentMethod == null) {
                response.put("status", "error");
                response.put("message", "Phương thức thanh toán không hợp lệ");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Kiểm tra số lượng sản phẩm
            for (var productOrderRequest : request.getProducts()) {
                if (!orderService.checkProductQuantity(productOrderRequest)) {
                    Product product = productService.findById(productOrderRequest.getProductId());
                    response.put("status", "error");
                    response.put("message", "Số lượng sản phẩm '" + product.getProductName() + "' không đủ để đặt hàng");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }

            if(request.getAddress() == null || request.getAddress().isEmpty()){
                response.put("status", "error");
                response.put("message", "Địa chỉ giao hàng không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if(request.getPhone() == null || request.getPhone().isEmpty() || !request.getPhone().matches("^(\\+84|0)\\d{9,10}$")){
                response.put("status", "error");
                response.put("message", "Số điện thoại không hợp lệ");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Thực hiện đặt hàng
            Map<String, Object> data = new HashMap<>();
            Order order = orderService.placeOrder(customer, paymentMethod, request.getAddress(), request.getPhone() , request.getNote(), request.getProducts(), request.getPromotionCode());
            if(order.getPaymentMethod() == PaymentMethod.BANKING){
                String vnpUrl = this.vNPayService.generateVNPayURL(order.getTotalAmount().doubleValue(), order.getPaymentRef());
                data.put("vnpUrl", vnpUrl);
            }
            response.put("status", "success");
            response.put("message", "Đặt hàng thành công");

            data.put("orderId", order.getId());
            data.put("totalAmount", order.getTotalAmount());
            data.put("orderStatus", order.getStatus());
            data.put("createdAt", order.getCreatedAt());
            data.put("shippingAddress", order.getAddress());
            data.put("paymentMethod", order.getPaymentMethod());
            data.put("note", order.getNote());
            data.put("user", customer.getId());
            data.put("orderItems", order.getOrderItems());

            response.put("data", data);


            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi đặt hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách đơn hàng của người dùng
     * @param authHeader
     * @return trả về danh sách đơn hàng của người dùng
     */
    @GetMapping("/getOrders")
    public ResponseEntity<Map<String, Object>> getOrders(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xem đơn hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            List<Order> orders = orderService.findByCustomer(customer);
            response.put("status", "success");
            response.put("message", "Lấy danh sách đơn hàng thành công");
            response.put("data", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng
     * @param authHeader
     * @param orderId
     * @param status
     * @return trả về thông tin đơn hàng đã cập nhật
     */
    @PutMapping("/updateOrderStatus/{orderId}")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer orderId,
            @RequestParam String status)
    {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để cập nhật trạng thái đơn hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            if (employee.getRole() == null || ( employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền cập nhật trạng thái đơn hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Order order = orderService.findById(orderId);
            if (order == null) {
                response.put("status", "error");
                response.put("message", "Đơn hàng không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            order = orderService.updateOrderStatus(orderId, OrderStatus.valueOf(status));
            response.put("status", "success");
            response.put("message", "Cập nhật trạng thái đơn hàng thành công");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Hủy đơn hàng
     * @param authHeader
     * @param orderId
     * @param reason
     * @return trả về thông tin đơn hàng đã hủy
     */
    @PutMapping("/cancelOrder/{orderId}")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer orderId,
            @RequestBody Map<String, String> reason) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            String reasonStr = reason.get("reason");
            if (reasonStr == null || reasonStr.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Lý do hủy đơn hàng không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để hủy đơn hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Order order = orderService.findById(orderId);
            if (order == null) {
                response.put("status", "error");
                response.put("message", "Đơn hàng không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!order.getCustomer().getId().equals(customer.getId())) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền hủy đơn hàng này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.UNPAID) {
                response.put("status", "error");
                response.put("message", "Chỉ có thể hủy đơn hàng ở trạng thái PENDING hoặc UNPAID");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            order = orderService.cancelOrder(order, reasonStr);
            response.put("status", "success");
            response.put("message", "Hủy đơn hàng thành công");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi hủy đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xử lý thanh toán qua VNPAY
     * @param totalPrice
     * @return trả về URL thanh toán của VNPAY
     * @throws UnsupportedEncodingException
     */
    @PostMapping("/vnpay_payment")
    public String handlePlaceOrder(@RequestParam("totalPrice") String totalPrice) throws UnsupportedEncodingException {
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        String vnpUrl = this.vNPayService.generateVNPayURL(Double.parseDouble(totalPrice), uuid);
        return vnpUrl;
    }

    @PostMapping("/repayment/{orderId}")
    public String handleRepayment(
            @PathVariable Integer orderId) throws UnsupportedEncodingException {
        Order order = orderService.findById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Order has already been paid");
        }
        final String uuid = UUID.randomUUID().toString().replace("-", "");
        order.setPaymentRef(uuid);
        orderService.save(order);
        String vnpUrl = this.vNPayService.generateVNPayURL(order.getTotalAmount().doubleValue(), order.getPaymentRef());
        return vnpUrl;
    }



    /**
     * Trang cảm ơn sau khi thanh toán
     * @param vnpayResponseCode
     * @param paymentRef
     * @return chuyển hướng đến trang kết quả đơn hàng
     */
    @GetMapping("/thanks")
    public RedirectView getThankYouPage(
            @RequestParam("vnp_ResponseCode") Optional<String> vnpayResponseCode,
            @RequestParam("vnp_TxnRef") Optional<String> paymentRef) {

        Order order = orderService.findByPaymentRef(paymentRef.orElse(""));
        if (order == null) {
            return new RedirectView(baseUrlFE + "/order-result?status=fail");
        }

        String redirectUrl;
        if (vnpayResponseCode.isPresent() && paymentRef.isPresent()) {
            if (vnpayResponseCode.get().equals("00")) {
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setStatus(OrderStatus.PENDING);
                orderService.save(order);
                redirectUrl = baseUrlFE + "/order-result?status=success&orderId=" + order.getId();
            } else {
                order.setPaymentStatus(PaymentStatus.FAILED);
                orderService.save(order);
                redirectUrl = baseUrlFE + "/order-result?status=fail&orderId=" + order.getId();
            }
            return new RedirectView(redirectUrl);
        }

        redirectUrl = baseUrlFE + "/order-result?status=fail&orderId=" + order.getId();
        return new RedirectView(redirectUrl);
    }

    /**
     * Hoàn tiền đơn hàng
     * @param paymentRef
     * @param transactionType
     * @return trả về kết quả hoàn tiền
     */
    @PostMapping("/refund")
    public ResponseEntity<Map<String, Object>> refundOrder(
            @RequestParam String paymentRef,
            @RequestParam(defaultValue = "02") String transactionType) // 02: toàn bộ
    {

        Map<String, Object> response = new HashMap<>();
        try {
            Order order = orderService.findByPaymentRef(paymentRef);
            if (order == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy đơn hàng với paymentRef = " + paymentRef);
                return ResponseEntity.badRequest().body(response);
            }

            if (order.getPaymentStatus() != PaymentStatus.PAID) {
                response.put("status", "error");
                response.put("message", "Đơn hàng chưa thanh toán, không thể hoàn tiền");
                return ResponseEntity.badRequest().body(response);
            }

            String transDate = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String result = vNPayService.refundVNPay(
                    order.getPaymentRef(),
                    order.getTotalAmount().longValue(),
                    transDate,
                    "adminRefund",
                    transactionType
            );

            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setStatus(OrderStatus.CANCELED);
            order.setCancelledAt(LocalDateTime.now());
            orderService.save(order);

            response.put("status", "success");
            response.put("message", "Hoàn tiền thành công cho đơn hàng " + order.getId());
            response.put("vnpResponse", result);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi hoàn tiền: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy tất cả đơn hàng (dành cho nhân viên và quản lý)
     * @param authHeader
     * @return trả về danh sách tất cả đơn hàng
     */
    @GetMapping("/getAllOrders")
    public ResponseEntity<Map<String, Object>> getAllOrders(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xem đơn hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (employee.getRole() == null || (employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền xem tất cả đơn hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            List<Order> orders = orderService.findAll();
            response.put("status", "success");
            response.put("message", "Lấy danh sách đơn hàng thành công");
            response.put("data", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin đơn hàng theo ID
     * @param authHeader
     * @param orderId
     * @return trả về thông tin đơn hàng
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xem đơn hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Order order = null;

            if(customer.getRole() == Role.STAFF || customer.getRole() == Role.MANAGER) {
                order = orderService.findById(orderId);
                if (order == null) {
                    response.put("status", "error");
                    response.put("message", "Đơn hàng không tồn tại");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            } else{
                order = orderService.findByIdAndUser(orderId, customer);
                if (order == null) {
                    response.put("status", "error");
                    response.put("message", "Nguoời dùng không có đơn hàng này hoặc đơn hàng không tồn tại");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            }

            response.put("status", "success");
            response.put("message", "Lấy thông tin đơn hàng thành công");
            response.put("data", order);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy thông tin đơn hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
