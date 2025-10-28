package vn.edu.iuh.fit.bookshop_be.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.models.Employee;
import vn.edu.iuh.fit.bookshop_be.models.Role;
import vn.edu.iuh.fit.bookshop_be.repositories.EmployeeRepository;
import vn.edu.iuh.fit.bookshop_be.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AvatarService avatarService;

    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee findById(int id) {
        return employeeRepository.findById(id).orElse(null);
    }

    public Employee findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Employee getEmployeeByToken(String authHeader) {
        try {
            // Kiểm tra xem header có đúng định dạng không
            if (authHeader == null) {
                return null;
            }
            String token = null;
            if(authHeader.startsWith("Bearer ")){
                token = authHeader.substring(7); // Bỏ qua "Bearer "
            }
            else {
                token = authHeader;
            }

            // Trích xuất username từ token
            String email = jwtUtil.extractEmail(token);
            Role role = Role.valueOf(jwtUtil.extractRole(token));
            if (email == null || role == null || (role != Role.STAFF && role != Role.MANAGER)) {
                return null;
            }

            // Tìm người dùng từ database
            Employee employee = employeeRepository.findByEmail(email);
            return employee;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Employee createAccountEmployee(String username, String email, String password, String phone, Role role) {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPasswordHash(password);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setPasswordHash(passwordEncoder.encode(employee.getPasswordHash()));
        employee.setRole(role);
        employee.setCreatedAt(LocalDateTime.now());
        String avatarUrl = avatarService.createAndUploadAvatar(employee.getUsername(), employee.getEmail());
        employee.setAvatarUrl(avatarUrl);
        employee.setActive(true);
        Employee savedEmployee =  employeeRepository.save(employee);
        return savedEmployee;
    }

    public boolean checkPassword(String passwordInput, String password) {
        return passwordEncoder.matches(passwordInput, password);
    }

    public Employee updateInfoAccount(Employee employee, String username, String phone, String email, Role role) {
        employee.setUsername(username);
        employee.setPhone(phone);
        employee.setEmail(email);
        employee.setRole(role);
        return employeeRepository.save(employee);
    }

    public Long countEmployees() {
        return employeeRepository.count();
    }
}
