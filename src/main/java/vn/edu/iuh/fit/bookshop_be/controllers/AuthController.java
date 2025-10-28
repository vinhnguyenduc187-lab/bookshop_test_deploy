package vn.edu.iuh.fit.bookshop_be.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import vn.edu.iuh.fit.bookshop_be.dtos.CreateAccountRequest;
import vn.edu.iuh.fit.bookshop_be.dtos.LoginRequest;
import vn.edu.iuh.fit.bookshop_be.dtos.SignUpRequest;
import vn.edu.iuh.fit.bookshop_be.models.Employee;
import vn.edu.iuh.fit.bookshop_be.models.Role;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.security.JwtUtil;
import vn.edu.iuh.fit.bookshop_be.services.CustomerService;
import vn.edu.iuh.fit.bookshop_be.services.EmployeeService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;

    public AuthController(CustomerService customerService, EmployeeService employeeService, JwtUtil jwtUtil) {
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Tìm kiếm người dùng theo email
     * @param email
     * @return ResponseEntity với thông tin kết quả
     */
    @GetMapping("/findByEmail")
    public ResponseEntity<Map<String, Object>> findByEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.findByEmail(email);
            if (customer == null) {
                response.put("message", "Không tìm thấy người dùng với email đã cho");
                return ResponseEntity.status(404).body(response);
            }
            response.put("message", "Tìm thấy người dùng với email đã cho");
            response.put("status", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("user", customer);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Đăng ký tài khoản
     * @param request
     * @return ResponseEntity với thông tin kết quả
     */
    @PostMapping("/signUp")
    @ResponseBody
    public ResponseEntity<Map<String ,Object>> signup(@RequestBody SignUpRequest request){
        Map<String, Object> response = new HashMap<>();
        try{
            // kiểm tra validation
            if(request.getEmail() == null || request.getUsername() == null || request.getPassword()== null || request.getPhone() == null){
                response.put("message", "Điền đầy đủ thông tin");
                return ResponseEntity.status(400).body(response);
            }
            //kiem tra so dien thoai
            if (request.getPhone() != null && !request.getPhone().matches("^(\\+84|0)\\d{9,10}$")) {
                response.put("message", "Số điện thoại không hợp lệ");
                return ResponseEntity.status(400).body(response);
            }
            if(customerService.findByEmail(request.getEmail()) != null){
                response.put("message" , "Email đã tồn tại");
                return ResponseEntity.status(400).body(response);
            }

            // Gọi UserService để đăng kí user
            customerService.signUp(request);
            response.put("message" , "Đăng kí tài khoản thành công");
            response.put("status" , "success");
            Map<String, Object> data = new HashMap<>();
            data.put("user", request);
            response.put("data", data);

            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            response.put("message" , e.getMessage());
            return ResponseEntity.status(400).body( response);
        } catch (Exception e) {
            response.put("message" , e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Gửi lại email xác thực
     * @param request
     * @return ResponseEntity với thông tin kết quả
     */
    @PostMapping("/resendVerificationEmail")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> response = new HashMap<>();
        try {
            if (email == null || email.isEmpty()) {
                response.put("message", "Email không được bỏ trống");
                return ResponseEntity.status(400).body(response);
            }

            Customer customer = customerService.findByEmail(email);
            if (customer == null) {
                response.put("message", "Không tìm thấy người dùng với email đã cho");
                return ResponseEntity.status(404).body(response);
            }
            if (customer.isEnabled()) {
                response.put("message", "Tài khoản đã được xác thực");
                return ResponseEntity.status(400).body(response);
            }
            customerService.sendVerificationEmail(email, customer.getVerificationCode());

            response.put("message", "Gửi lại email xác thực thành công. Vui lòng kiểm tra email của bạn.");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Gửi mã OTP để đặt lại mật khẩu
     * @param request
     * @return ResponseEntity với thông tin kết quả
     */
    @PostMapping("/sendResetPasswordOtp")
    public ResponseEntity<Map<String, String>> sendResetPasswordOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> response = new HashMap<>();
        try {
            if (email == null || email.isEmpty()) {
                response.put("message", "Email không được bỏ trống");
                return ResponseEntity.status(400).body(response);
            }

            Customer customer = customerService.findByEmail(email);
            if (customer == null) {
                response.put("message", "Không tìm thấy người dùng với email đã cho");
                return ResponseEntity.status(404).body(response);
            }
            String otp = customerService.sendResetPasswordOtp(email);
            customer.setVerificationCode(otp);
            customerService.save(customer);
            response.put("message", "Gửi mã thành công. Vui lòng kiểm tra email của bạn.");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Đặt lại mật khẩu
     * @param request
     * @return ResponseEntity với thông tin kết quả
     */
    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        Map<String, String> response = new HashMap<>();
        try {
            if (email == null || email.isEmpty() || otp == null || otp.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                response.put("message", "Email, mã OTP và mật khẩu mới không được bỏ trống");
                return ResponseEntity.status(400).body(response);
            }
            Customer customer = customerService.findByEmail(email);
            if (customer == null) {
                response.put("message", "Không tìm thấy người dùng với email đã cho");
                return ResponseEntity.status(404).body(response);
            }
            if (!otp.equals(customer.getVerificationCode())) {
                response.put("message", "Mã OTP không đúng");
                return ResponseEntity.status(400).body(response);
            }
            customerService.resetPassword(customer, newPassword);
            response.put("message", "Đặt lại mật khẩu thành công");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Xóa mã OTP sau khi đặt lại mật khẩu thành công
     * @param request
     * @return ResponseEntity với thông tin kết quả
     */
    @PostMapping("/removeResetPasswordOtp")
    public ResponseEntity<Map<String, String>> removeResetPasswordOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> response = new HashMap<>();
        try {
            if (email == null || email.isEmpty()) {
                response.put("message", "Email không được bỏ trống");
                return ResponseEntity.status(400).body(response);
            }
            Customer customer = customerService.findByEmail(email);
            if (customer == null) {
                response.put("message", "Không tìm thấy người dùng với email đã cho");
                return ResponseEntity.status(404).body(response);
            }
            customer.setVerificationCode(null);
            customerService.save(customer);
            response.put("message", "Xóa mã OTP thành công");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Xác thực tài khoản người dùng
     * @param verificationCode
     * @return RedirectView chuyển hướng người dùng đến trang xác thực thành công hoặc thất bại
     */
    @GetMapping("/verify")
    public RedirectView verifyUser(@RequestParam("code") String verificationCode) {
        try{
            Customer existingCustomer = customerService.findByVerificationCode(verificationCode);
            boolean verified = customerService.verifyUser(verificationCode);
            String redirectUrl;

            if (verified) {
                String token = jwtUtil.generateAccessToken(existingCustomer.getEmail(), Role.CUSTOMER.toString());

                // Redirect về FE kèm token trên URL
                redirectUrl = "http://localhost:5173/verify-success?token=" + token;
            } else {
                redirectUrl = "http://localhost:5173/verify-failed";
            }

            return new RedirectView(redirectUrl);
        } catch (Exception e) {
            return new RedirectView("http://localhost:5173/verify-failed");
        }
    }



    /**
     * Đăng nhập tài khoản khach hàng
     * @param request
     * @return ResponseEntity với thông tin kết quả
     */
    @PostMapping("/loginCustomer")
    public ResponseEntity<Map<String, Object>> loginCustomer(@RequestBody LoginRequest request) {
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setPasswordHash(request.getPassword());

        Map<String, Object> response = new HashMap<>();
        try {
            // kiểm tra username hoặc password có bị null không
            if (customer.getEmail() == null || customer.getPasswordHash() == null) {
                response.put("message" , "Tài khoản và mật khẩu không được bỏ trống");
                return ResponseEntity.status(400).body(response);
            }
            // kiểm tra user có trong db không
            Customer existingCustomer = customerService.findByEmail(customer.getEmail());
            if (existingCustomer == null) {
                response.put("message" , "Tài khoản hoặc mật khẩu không chính xác");
                return ResponseEntity.status(401).body(response);
            }

            if (customerService.checkPassword(customer.getPasswordHash(), existingCustomer.getPasswordHash())) {
                String accessToken = jwtUtil.generateAccessToken(existingCustomer.getEmail() , existingCustomer.getRole().toString());
                String refreshToken = jwtUtil.generateRefreshToken(existingCustomer.getEmail());
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refreshToken);

                // Tạo map user để chứa thông tin người dùng
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("username", existingCustomer.getUsername());
                userInfo.put("email", existingCustomer.getEmail());
                userInfo.put("id", existingCustomer.getId());
                userInfo.put("avatar", existingCustomer.getAvatarUrl());
                userInfo.put("role", existingCustomer.getRole());
                userInfo.put("phone", existingCustomer.getPhone());
                userInfo.put("isEnabled", existingCustomer.isEnabled());

                response.put("message", "Đăng nhập thành công");
                response.put("status", "success");
                Map<String, Object> data = new HashMap<>();
                data.put("tokens", tokens);
                data.put("customer", userInfo);

                response.put("data", data);


                return ResponseEntity.ok(response);

            } else {
                response.put("message","Lỗi đăng nhập. Tài khoản hoặc mật khẩu không chính xác");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("message" , e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Đăng nhập tài khoản nhân viên
     * @param request
     * @return ResponseEntity với thông tin kết quả
     */
    @PostMapping("/loginEmployee")
    public ResponseEntity<Map<String, Object>> loginEmployee(@RequestBody LoginRequest request) {
        Employee employee = new Employee();
        employee.setEmail(request.getEmail());
        employee.setPasswordHash(request.getPassword());
        Map<String, Object> response = new HashMap<>();
        try {
            // kiểm tra username hoặc password có bị null không
            if (employee.getEmail() == null || employee.getPasswordHash() == null) {
                response.put("message" , "Tài khoản và mật khẩu không được bỏ trống");
                return ResponseEntity.status(400).body(response);
            }
            // kiểm tra user có trong db không
            Employee existingEmployee = employeeService.findByEmail(employee.getEmail());

            if (existingEmployee == null) {
                response.put("message" , "Tài khoản hoặc mật khẩu không chính xác");
                return ResponseEntity.status(401).body(response);
            }

            if (employeeService.checkPassword(employee.getPasswordHash(), existingEmployee.getPasswordHash())) {
                String accessToken = jwtUtil.generateAccessToken(existingEmployee.getEmail() , existingEmployee.getRole().toString());
                String refreshToken = jwtUtil.generateRefreshToken(existingEmployee.getEmail());
                Map<String, String> tokens = new HashMap<>();
                tokens.put("accessToken", accessToken);
                tokens.put("refreshToken", refreshToken);

                // Tạo map user để chứa thông tin người dùng
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("username", existingEmployee.getUsername());
                userInfo.put("email", existingEmployee.getEmail());
                userInfo.put("id", existingEmployee.getId());
                userInfo.put("avatar", existingEmployee.getAvatarUrl());
                userInfo.put("role", existingEmployee.getRole());
                userInfo.put("phone", existingEmployee.getPhone());
                userInfo.put("isActive", existingEmployee.isActive());

                response.put("message", "Đăng nhập thành công");
                response.put("status", "success");
                Map<String, Object> data = new HashMap<>();
                data.put("tokens", tokens);
                data.put("employee", userInfo);

                response.put("data", data);


                return ResponseEntity.ok(response);

            } else {
                response.put("message","Tài khoản hoặc mật khẩu không chính xác");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("message" , e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Lấy thông tin người dùng từ token
     * @param authHeader
     * @return ResponseEntity với thông tin kết quả
     */
    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            // Kiểm tra xem người dùng có tồn tại không
            if (customer == null && employee == null) {
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }
            if (customer != null) {
                // Trả về thông tin người dùng
                response.put("message", "Lấy thông tin người dùng thành công");
                response.put("status", "success");
                Customer customerRender = new Customer();
                customerRender.setId(customer.getId());
                customerRender.setUsername(customer.getUsername());
                customerRender.setEmail(customer.getEmail());
                customerRender.setRole(customer.getRole());
                customerRender.setAvatarUrl(customer.getAvatarUrl());
                customerRender.setPhone(customer.getPhone());

                Map<String, Object> data = new HashMap<>();
                data.put("user", customerRender);
                response.put("data", data);
            } else {
                // Trả về thông tin nhân viên
                response.put("message", "Lấy thông tin nhân viên thành công");
                response.put("status", "success");
                Employee employeeRender = new Employee();
                employeeRender.setId(employee.getId());
                employeeRender.setUsername(employee.getUsername());
                employeeRender.setEmail(employee.getEmail());
                employeeRender.setRole(employee.getRole());
                employeeRender.setAvatarUrl(employee.getAvatarUrl());
                employeeRender.setPhone(employee.getPhone());

                Map<String, Object> data = new HashMap<>();
                data.put("user", employeeRender);
                response.put("data", data);
            }





            // Trả về thông tin người dùng
////           Map<String, Object> userInfo = new HashMap<>();
//            response.put("message", "Lấy thông tin người dùng thành công");
//            response.put("status", "success");
//            Customer customerRender = new Customer();
//            customerRender.setId(customer.getId());
//            customerRender.setUsername(customer.getUsername());
//            customerRender.setEmail(customer.getEmail());
//            customerRender.setRole(customer.getRole());
//            customerRender.setAvatarUrl(customer.getAvatarUrl());
//            customerRender.setPhone(customer.getPhone());
//
//            Map<String, Object> data = new HashMap<>();
//            data.put("user", customerRender);
//            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Thêm tài khoản mới (chỉ dành cho STAFF và MANAGER)
     * @param request
     * @param authHeader
     * @return ResponseEntity với thông tin kết quả
     */
    @PostMapping("/createAccountCustomer")
    public ResponseEntity<Map<String, Object>> createAccountCustomer(
            @RequestBody CreateAccountRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            // Kiểm tra xem người dùng có tồn tại không
            if (employee.getRole() == null || ( employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(403).body(response);
            }
            // kiểm tra validation
            if (request.getEmail() == null || request.getUsername() == null || request.getPassword() == null || request.getPhone() == null) {
                response.put("message", "Điền đầy đủ thông tin");
                return ResponseEntity.status(400).body(response);
            }

            if(customerService.findByEmail(request.getEmail()) != null){
                response.put("message", "Email khách hàng đã tồn tại");
                return ResponseEntity.status(400).body(response);
            }

            if(request.getEmail() != null && !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.put("message", "Email không hợp lệ");
                return ResponseEntity.status(400).body(response);
            }

            //kiem tra so dien thoai
            if (request.getPhone() != null && !request.getPhone().matches("^(\\+84|0)\\d{9,10}$")) {
                response.put("message", "Số điện thoại không hợp lệ");
                return ResponseEntity.status(400).body(response);
            }

            // Gọi UserService để đăng kí user
            customerService.createAccountCustomer(request.getUsername(), request.getEmail(), request.getPassword(), request.getPhone());
            response.put("message", "Thêm tài khoản khách hàng thành công");
            response.put("status", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("customer", request);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/createAccountEmployee")
    public ResponseEntity<Map<String, Object>> createAccountEmployee(
            @RequestBody CreateAccountRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            // Kiểm tra xem người dùng có tồn tại không
            if (employee.getRole() != Role.MANAGER) {
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(403).body(response);
            }
            // kiểm tra validation
            if (request.getEmail() == null || request.getUsername() == null || request.getPassword() == null || request.getPhone() == null || request.getRole() == null) {
                response.put("message", "Điền đầy đủ thông tin");
                return ResponseEntity.status(400).body(response);
            }

            if(employeeService.findByEmail(request.getEmail()) != null){
                response.put("message", "Email nhân viên đã tồn tại");
                return ResponseEntity.status(400).body(response);
            }

            if(request.getEmail() != null && !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                response.put("message", "Email không hợp lệ");
                return ResponseEntity.status(400).body(response);
            }

            //kiem tra so dien thoai
            if (request.getPhone() != null && !request.getPhone().matches("^(\\+84|0)\\d{9,10}$")) {
                response.put("message", "Số điện thoại không hợp lệ");
                return ResponseEntity.status(400).body(response);
            }

            // Gọi UserService để đăng kí user
            employeeService.createAccountEmployee(request.getUsername(), request.getEmail(), request.getPassword(), request.getPhone(), request.getRole());
            response.put("message", "Thêm tài khoản nhân viên thành công");
            response.put("status", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("employee", request);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }









}
