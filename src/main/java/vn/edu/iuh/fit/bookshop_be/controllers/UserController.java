package vn.edu.iuh.fit.bookshop_be.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.bookshop_be.dtos.AddressRequest;
import vn.edu.iuh.fit.bookshop_be.dtos.ChangePasswordRequest;
import vn.edu.iuh.fit.bookshop_be.dtos.UpdateActiveRequest;
import vn.edu.iuh.fit.bookshop_be.dtos.UpdateInfoRequest;
import vn.edu.iuh.fit.bookshop_be.models.Address;
import vn.edu.iuh.fit.bookshop_be.models.Employee;
import vn.edu.iuh.fit.bookshop_be.models.Role;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.security.JwtUtil;
import vn.edu.iuh.fit.bookshop_be.services.AddressService;
import vn.edu.iuh.fit.bookshop_be.services.CustomerService;
import vn.edu.iuh.fit.bookshop_be.services.EmployeeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;
    private final Cloudinary cloudinary;
    private final AddressService addressService;

    public UserController(CustomerService customerService, EmployeeService employeeService, JwtUtil jwtUtil, Cloudinary cloudinary, AddressService addressService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.jwtUtil = jwtUtil;
        this.cloudinary = cloudinary;
        this.addressService = addressService;
    }

    /**
     * Cập nhật ảnh đại diện cho người dùng
     * @param authHeader
     * @param image
     * @return trả về thông tin người dùng sau khi cập nhật ảnh đại diện
     */
    @PostMapping("/updateAvatar")
    public ResponseEntity<Map<String, Object>> updateAvatar(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);

            if (customer == null ) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để cập nhật ảnh đại diện");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Kiểm tra có gửi ảnh không
            if (image == null || image.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Vui lòng chọn ảnh để cập nhật");
                return ResponseEntity.badRequest().body(response);
            }

            String folderName =  "avatars/" + customer.getEmail() ;
            // Upload ảnh lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(image.getBytes(),
                    ObjectUtils.asMap("folder", folderName));

            String imageUrl = (String) uploadResult.get("secure_url");

            // Cập nhật avatar vào user
            customer.setAvatarUrl(imageUrl);
            customerService.save(customer);

            // Trả kết quả
            response.put("status", "success");
            response.put("message", "Cập nhật ảnh đại diện thành công");
            Customer customerRender = new Customer();
            customerRender.setId(customer.getId());
            customerRender.setUsername(customer.getUsername());
            customerRender.setEmail(customer.getEmail());
            customerRender.setRole(customer.getRole());
            customerRender.setAvatarUrl(customer.getAvatarUrl());

            Map<String, Object> data = new HashMap<>();
            data.put("user", customerRender);
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi cập nhật ảnh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    /*** Cập nhật thông tin người dùng
     * @param authHeader
     * @param request
     * @return trả về thông tin người dùng sau khi cập nhật
     */
    @PutMapping("/updateInfo")
    public ResponseEntity<Map<String, Object>> updateInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateInfoRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để cập nhật thông tin");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if( request.getUsername() == null || request.getUsername().isEmpty() || request.getPhone() == null || request.getPhone().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Nhập đầy đủ thông tin để cập nhật");
                return ResponseEntity.badRequest().body(response);
            }

            if( !request.getPhone().matches("^0[0-9]{9,10}$")) {
                response.put("status", "error");
                response.put("message", "Số điện thoại phải bắt đầu bằng số 0 và có độ dài từ 10 đến 11 chữ số");
                return ResponseEntity.badRequest().body(response);
            }

            // Cập nhật thông tin người dùng
            customer.setUsername(request.getUsername());
            customer.setPhone(request.getPhone());
            customerService.save(customer);

            response.put("status", "success");
            response.put("message", "Cập nhật thông tin thành công");
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

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi cập nhật thông tin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Đổi mật khẩu cho người dùng
     * @param authHeader
     * @param request
     * @return trả về thông tin người dùng sau khi đổi mật khẩu
     */
    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChangePasswordRequest request
            ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để đổi mật khẩu");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String currentPassword = request.getCurrentPassword();
            String newPassword = request.getNewPassword();

            if (!customerService.checkPassword(currentPassword, customer.getPasswordHash())) {
                response.put("status", "error");
                response.put("message", "Mật khẩu hiện tại không đúng");
                return ResponseEntity.badRequest().body(response);
            }


            if (currentPassword == null || newPassword == null || currentPassword.isEmpty() || newPassword.isEmpty() ) {
                response.put("status", "error");
                response.put("message", "Vui lòng điền đầy đủ thông tin mật khẩu");
                return ResponseEntity.badRequest().body(response);
            }


            Customer customerChange = customerService.changePassword(customer, newPassword);

            response.put("status", "success");
            response.put("message", "Đổi mật khẩu thành công");
            Map<String, Object> data = new HashMap<>();
            Customer customerRender = new Customer();
            customerRender.setId(customerChange.getId());
            customerRender.setUsername(customerChange.getUsername());
            customerRender.setEmail(customerChange.getEmail());
            customerRender.setRole(customerChange.getRole());
            customerRender.setAvatarUrl(customerChange.getAvatarUrl());
            customerRender.setPhone(customerChange.getPhone());
            data.put("user", customerRender);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi đổi mật khẩu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Thêm địa chỉ mới cho người dùng
     * @param request
     * @param authHeader
     * @return danh sách địa chỉ của người dùng sau khi thêm
     */
    @PostMapping("/addAddress")
    public ResponseEntity<Map<String, Object>> addAddress(@RequestBody AddressRequest request, @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thêm địa chỉ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Address address = new Address();
            address.setStreet(request.getStreet());
            address.setWard(request.getWard());
            address.setDistrict(request.getDistrict());
            address.setCity(request.getCity());
            address.setNote(request.getNote());
            address.setCustomer(customer);
            Address savedAddress = addressService.save(address);

            response.put("status", "success");
            response.put("message", "Thêm địa chỉ thành công");
            Map<String, Object> data = new HashMap<>();
            Customer customerRender = new Customer();
            customerRender.setId(customer.getId());
            customerRender.setUsername(customer.getUsername());
            customerRender.setEmail(customer.getEmail());
            customerRender.setRole(customer.getRole());
            customerRender.setAvatarUrl(customer.getAvatarUrl());
            List<Address> addresses = customer.getAddresses();
            addresses.add(savedAddress);
            customerRender.setAddresses(addresses);
            data.put("user", customerRender);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi thêm địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa địa chỉ của người dùng
     * @param authHeader
     * @return danh sách địa chỉ của người dùng sau khi xóa
     */
    @DeleteMapping("/deleteAddress/{addressId}")
    public ResponseEntity<Map<String, Object>> deleteAddress(@PathVariable("addressId") Integer id, @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xóa địa chỉ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Address address = addressService.findById(id);
            if (address == null) {
                response.put("status", "error");
                response.put("message", "Địa chỉ không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!address.getCustomer().getId().equals(customer.getId())) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền xóa địa chỉ này");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            addressService.deleteById(id);
            response.put("status", "success");
            response.put("message", "Xóa địa chỉ thành công");
            Customer customerRender = new Customer();
            customerRender.setId(customer.getId());
            customerRender.setUsername(customer.getUsername());
            customerRender.setEmail(customer.getEmail());
            customerRender.setRole(customer.getRole());
            customerRender.setAvatarUrl(customer.getAvatarUrl());
            List<Address> addresses = customer.getAddresses();
            addresses.removeIf(addr -> addr != null && addr.getId() == id);
            customerRender.setAddresses(addresses);
            Map<String, Object> data = new HashMap<>();
            data.put("customer", customerRender);
            response.put("data", data);


            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi xóa địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    /**
     * Cập nhật địa chỉ của người dùng
     * @param id
     * @param authHeader
     * @param request
     * @return danh sách địa chỉ của người dùng sau khi cập nhật
     */
    @PutMapping("/updateAddress/{addressId}")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @PathVariable("addressId") Integer id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AddressRequest request)
    {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thêm địa chỉ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Address existingAddress = addressService.findByIdAndUser(id, customer);
            if (existingAddress == null) {
                response.put("status", "error");
                response.put("message", "Địa chỉ không tồn tại hoặc bạn không có quyền sửa địa chỉ này");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Address address = addressService.findById(id);
            address.setStreet(request.getStreet());
            address.setWard(request.getWard());
            address.setDistrict(request.getDistrict());
            address.setCity(request.getCity());
            address.setNote(request.getNote());
            address.setCustomer(customer);
            addressService.save(address);

            response.put("status", "success");
            response.put("message", "Thêm cập nhật thành công");
            Map<String, Object> data = new HashMap<>();
            Customer customerRender = new Customer();
            customerRender.setId(customer.getId());
            customerRender.setUsername(customer.getUsername());
            customerRender.setEmail(customer.getEmail());
            customerRender.setRole(customer.getRole());
            customerRender.setAvatarUrl(customer.getAvatarUrl());
            List<Address> addresses = customer.getAddresses();
            addresses.add(address);
            customerRender.setAddresses(addresses);
            data.put("user", customerRender);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi cập nhật địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách địa chỉ của người dùng
     * @param authHeader
     * @return danh sách địa chỉ của người dùng
     */
    @GetMapping("/addresses")
    public ResponseEntity<Map<String, Object>> getAddresses(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xem địa chỉ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            List<Address> addresses = addressService.findByCustomer(customer);
            response.put("status", "success");
            response.put("message", "Lấy danh sách địa chỉ thành công");
            Map<String, Object> data = new HashMap<>();
            List<Address> addressesRender = addresses.stream().map(u -> {
                Address address = new Address();
                address.setId(u.getId());
                address.setStreet(u.getStreet());
                address.setWard(u.getWard());
                address.setDistrict(u.getDistrict());
                address.setCity(u.getCity());
                address.setNote(u.getNote());
                return address;
            }).toList();
            data.put("addresses", addressesRender);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin địa chỉ theo ID
     * @param id
     * @param authHeader
     * @return thông tin địa chỉ
     */
    @GetMapping("/getAddress/{addressId}")
    public ResponseEntity<Map<String, Object>> getAddressById(
            @PathVariable("addressId") Integer id,
            @RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xem địa chỉ");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Address address = addressService.findByIdAndUser(id, customer);
            if (address == null) {
                response.put("status", "error");
                response.put("message", "Địa chỉ không tồn tại hoặc bạn không có quyền xem địa chỉ này");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("status", "success");
            response.put("message", "Lấy địa chỉ thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("address", address);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy địa chỉ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật thông tin tài khoản (chỉ dành cho STAFF và MANAGER)
     * @param request
     * @param authHeader
     * @param id
     * @return ResponseEntity với thông tin kết quả
     */
    @PutMapping("/updateInfoAccount/{id}/{userRole}")
    public ResponseEntity<Map<String, Object>> updateInfoAccount(
            @RequestBody UpdateInfoRequest request,
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Integer id,
            @PathVariable("userRole") String userRole
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);

            // Kiểm tra xem người dùng có tồn tại không
            if (employee == null || (employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(404).body(response);
            }

            String username = request.getUsername();
            String phone = request.getPhone();
            String email = request.getEmail();
            Role role = request.getRole();

            // kiểm tra validation
            if (username == null || phone == null || email == null || role == null || username.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Điền đầy đủ thông tin");
                return ResponseEntity.status(400).body(response);
            }

            //kiem tra so dien thoai
            if (phone != null && !phone.matches("^(\\+84|0)\\d{9,10}$")) {
                response.put("status", "error");
                response.put("message", "Số điện thoại không hợp lệ");
                return ResponseEntity.status(400).body(response);
            }

            if(userRole.equalsIgnoreCase("Customer")){
                Customer customer = customerService.findById(id);
                if (customer == null) {
                    response.put("message", "Người dùng không tồn tại");
                    return ResponseEntity.status(404).body(response);
                }

                // Gọi UserService để cập nhật thông tin user
                customerService.updateInfoAccount(customer, username, phone, email);
                response.put("message", "Cập nhật thông tin tài khoản thành công");
                response.put("status", "success");
                Map<String, Object> data = new HashMap<>();
                data.put("customer", customerService.findById(customer.getId()));
                response.put("data", data);
            } else if (userRole.equalsIgnoreCase("Employee")) {
                Employee employeeUpdate = employeeService.findById(id);
                if (employeeUpdate == null) {
                    response.put("message", "Người dùng không tồn tại");
                    return ResponseEntity.status(404).body(response);
                }

                // Gọi UserService để cập nhật thông tin user
                employeeService.updateInfoAccount(employeeUpdate, username, phone, email, role);
                response.put("message", "Cập nhật thông tin tài khoản thành công");
                response.put("status", "success");
                Map<String, Object> data = new HashMap<>();
                data.put("employee", employeeService.findById(employeeUpdate.getId()));
                response.put("data", data);
            } else {
                response.put("message", "Vai trò người dùng không hợp lệ");
                return ResponseEntity.status(400).body(response);
            }

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
     * Lấy danh sách tất cả khách hàng (chỉ dành cho STAFF và MANAGER)
     * @param authHeader
     * @return ResponseEntity với thông tin kết quả
     */
    @GetMapping("/getAllCustomers")
    public ResponseEntity<Map<String, Object>> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            // Kiểm tra xem người dùng có tồn tại không
            if (employee.getRole() == null || (employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(403).body(response);
            }
            List<Customer> customers = customerService.getAllCustomer();
            response.put("message", "Lấy danh sách khách hàng thành công");
            response.put("status", "success");
            Map<String, Object> data = new HashMap<>();
            List<Customer> usersRender = customers.stream().map(u -> {
                Customer customerRender = new Customer();
                customerRender.setId(u.getId());
                customerRender.setUsername(u.getUsername());
                customerRender.setEmail(u.getEmail());
                customerRender.setRole(u.getRole());
                customerRender.setAvatarUrl(u.getAvatarUrl());
                customerRender.setPhone(u.getPhone());
                customerRender.setCreatedAt(u.getCreatedAt());
                customerRender.setActive(u.isActive());
                customerRender.setEnabled(u.isEnabled());
                return customerRender;
            }).toList();
            data.put("users", usersRender);

            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Lấy danh sách tất cả nhân viên và quản lý (chỉ dành cho MANAGER)
     * @param authHeader
     * @return ResponseEntity với thông tin kết quả
     */
    @GetMapping("/employees")
    public ResponseEntity<Map<String, Object>> getStaffAndManagers(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            // Kiểm tra xem người dùng có tồn tại không
            if (employee.getRole() == null || (employee.getRole() != Role.MANAGER)) {
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(403).body(response);
            }
            List<Employee> employees = employeeService.findAll();
            response.put("message", "Lấy danh sách nhân viên và quản lý thành công");
            response.put("status", "success");
            Map<String, Object> data = new HashMap<>();
            List<Employee> usersRender = employees.stream().map(u -> {
                Employee employeeRender = new Employee();
                employeeRender.setId(u.getId());
                employeeRender.setUsername(u.getUsername());
                employeeRender.setEmail(u.getEmail());
                employeeRender.setRole(u.getRole());
                employeeRender.setAvatarUrl(u.getAvatarUrl());
                employeeRender.setPhone(u.getPhone());
                employeeRender.setCreatedAt(u.getCreatedAt());
                employeeRender.setActive(u.isActive());
//                customerRender.setEnabled(u.isEnabled());
                return employeeRender;
            }).toList();
            data.put("users", usersRender);

            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/updateActiveAccount/{id}")
    public ResponseEntity<Map<String, Object>> updateActive(
            @RequestBody UpdateActiveRequest request,
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            // Kiểm tra xem người dùng có tồn tại không
            if (employee == null || employee.getRole() != Role.MANAGER) {
                response.put("message", "Bạn không có quyền thực hiện hành động này");
                return ResponseEntity.status(404).body(response);
            }

            Employee employeeUpdate = employeeService.findById(id);
            if (employeeUpdate == null) {
                response.put("message", "Người dùng không tồn tại");
                return ResponseEntity.status(404).body(response);
            }

            Boolean isActive = request.getIsActive();
            if (isActive == null) {
                response.put("status", "error");
                response.put("message", "Điền đầy đủ thông tin");
                return ResponseEntity.status(400).body(response);
            }
            employeeUpdate.setActive(isActive);
            employeeService.save(employeeUpdate);
            response.put("message", "Cập nhật trạng thái tài khoản thành công");
            response.put("status", "success");
            Map<String, Object> data = new HashMap<>();
            data.put("user", employeeService.findById(employeeUpdate.getId()));
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

}
