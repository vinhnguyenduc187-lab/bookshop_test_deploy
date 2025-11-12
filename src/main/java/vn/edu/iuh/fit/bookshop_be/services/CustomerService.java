package vn.edu.iuh.fit.bookshop_be.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.dtos.SignUpRequest;
import vn.edu.iuh.fit.bookshop_be.models.Employee;
import vn.edu.iuh.fit.bookshop_be.models.Role;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.repositories.CustomerRepository;
import vn.edu.iuh.fit.bookshop_be.security.JwtUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${base_url}")
    private String baseUrl;


    @Value("${app.mail.from}")
    private String fromEmail;

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer findById(Integer id) {
        return customerRepository.findById(id).orElse(null);
    }

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public boolean checkPassword(String passwordInput, String password) {
        return passwordEncoder.matches(passwordInput, password);
    }

    public Customer getCustomerByToken(String authHeader) {
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
            if (email == null || role == null || role != Role.CUSTOMER) {
                return null;
            }

            // Tìm người dùng từ database
            Customer customer = customerRepository.findByEmail(email);
            return customer;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Customer signUp(SignUpRequest request) {
        Customer customer = new Customer();
        customer.setUsername(request.getUsername());
        customer.setPasswordHash(request.getPassword());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setEnabled(false);
        customer.setVerificationCode(UUID.randomUUID().toString());
        if(customerRepository.findByEmail(customer.getEmail()) != null){
            throw new IllegalArgumentException("Email đã tồn tại ");
        }
        customer.setPasswordHash(passwordEncoder.encode(customer.getPasswordHash()));
        customer.setRole(Role.CUSTOMER);
        customer.setCreatedAt(LocalDateTime.now());
        String avatarUrl = avatarService.createAndUploadAvatar(customer.getUsername(), customer.getEmail());
        customer.setAvatarUrl(avatarUrl);
        customer.setActive(false);
        Customer savedCustomer =  customerRepository.save(customer);
        sendVerificationEmail(customer.getEmail(), customer.getVerificationCode());
        return savedCustomer;
    }


    public String sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            // Encode để tránh lỗi ký tự lạ trong code
            String encoded = URLEncoder.encode(verificationCode, StandardCharsets.UTF_8);
            String link = baseUrl + "/api/auth/verify?code=" + encoded;

            // Fallback plain text (phòng khi mail client không hỗ trợ HTML)
            String plainText =
                    "Chào bạn,\n\n" +
                            "Cảm ơn bạn đã đăng ký. Vui lòng xác thực tài khoản để bắt đầu sử dụng dịch vụ.\n\n" +
                            "Nhấp vào liên kết sau: " + link + "\n\n" +
                            "Nếu bạn không thực hiện yêu cầu, hãy bỏ qua email này.";

            // HTML Template với placeholder {{verification_link}}
            String htmlTemplate = """
            <!doctype html>
            <html lang="vi">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width,initial-scale=1">
              <title>Xác nhận đăng ký</title>
              <style>
                body {
                  background: #f6f8fb;
                  margin: 0;
                  padding: 0;
                  font-family: Inter, Segoe UI, Arial, sans-serif;
                  color: #1f2937;
                }
                .container {
                  max-width: 560px;
                  margin: 0 auto;
                  padding: 24px;
                }
                .card {
                  background: #ffffff;
                  border-radius: 16px;
                  box-shadow: 0 6px 18px rgba(0,0,0,.06);
                  overflow: hidden;
                }
                .header {
                  background: linear-gradient(135deg, #4f46e5, #06b6d4);
                  padding: 24px;
                  color: #fff !important;
                }
                .brand {
                  font-size: 18px;
                  font-weight: 700;
                  letter-spacing: .4px;
                  color: #ffffff !important;
                }
                .content {
                  padding: 24px;
                }
                .title {
                  font-size: 20px;
                  margin: 0 0 8px;
                  font-weight: bold;
                }
                .muted {
                  color: #6b7280;
                  margin: 0 0 20px;
                  line-height: 1.6;
                }
                .btn {
                  display: inline-block;
                  text-decoration: none;
                  padding: 12px 18px;
                  border-radius: 10px;
                  background: #4f46e5;
                  color: #fff !important;
                  font-weight: 600;
                }
                .btn:hover {
                  opacity: .95;
                }
                .link {
                  word-break: break-all;
                  font-size: 12px;
                  color: #2563eb;
                  margin-top: 12px;
                }
                .footer {
                  padding: 16px 24px;
                  border-top: 1px solid #eef2f7;
                  color: #9ca3af;
                  font-size: 12px;
                }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="card">
                  <!-- HEADER -->
                  <div class="header">
                    <div class="brand">📚 BookShop • Xác thực tài khoản</div>
                  </div>

                  <!-- CONTENT -->
                  <div class="content">
                    <h1 class="title">Chào bạn,</h1>
                    <p class="muted">
                      Cảm ơn bạn đã đăng ký. Vui lòng xác thực tài khoản để bắt đầu sử dụng dịch vụ.
                    </p>
                    <p style="margin:16px 0 24px">
                      <a class="btn" href="{{verification_link}}" target="_blank" rel="noopener">
                        Xác nhận tài khoản
                      </a>
                    </p>
                  </div>

                  <!-- FOOTER -->
                  <div class="footer">
                    Email này được gửi tự động, vui lòng không trả lời.<br>
                    Nếu bạn không thực hiện yêu cầu, hãy bỏ qua email.
                  </div>
                </div>
              </div>
            </body>
            </html>
            """;

            // Thay thế placeholder
            String html = htmlTemplate.replace("{{verification_link}}", link);

            // Tạo mail MIME để gửi kèm HTML
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đăng ký tài khoản");
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }

            // setText(plain, html) => ưu tiên HTML, fallback text
            helper.setText(plainText, html);

            mailSender.send(mime);

            return verificationCode;
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email xác thực: " + e.getMessage(), e);
        }
    }

    public String sendResetPasswordOtp(String toEmail) {
        try {
            // 🔹 Tạo mã OTP 6 số ngẫu nhiên
            String otp = String.format("%06d", new Random().nextInt(999999));

            // 🔹 Nội dung plain text (phòng khi mail client không hỗ trợ HTML)
            String plainText =
                    "Xin chào,\n\n" +
                            "Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản BookShop của mình.\n\n" +
                            "Mã OTP của bạn là: " + otp + "\n\n" +
                            "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
                            "Trân trọng,\nBookShop Support";

            // 🔹 Giao diện HTML đẹp, thân thiện
            String htmlTemplate = """
        <!doctype html>
        <html lang="vi">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width,initial-scale=1">
          <title>Mã OTP đặt lại mật khẩu</title>
          <style>
            body {
              background: #f6f8fb;
              margin: 0;
              padding: 0;
              font-family: Inter, Segoe UI, Arial, sans-serif;
              color: #1f2937;
            }
            .container {
              max-width: 560px;
              margin: 0 auto;
              padding: 24px;
            }
            .card {
              background: #ffffff;
              border-radius: 16px;
              box-shadow: 0 6px 18px rgba(0,0,0,.06);
              overflow: hidden;
            }
            .header {
              background: linear-gradient(135deg, #06b6d4, #3b82f6);
              padding: 24px;
              color: #fff !important;
            }
            .brand {
              font-size: 18px;
              font-weight: 700;
              letter-spacing: .4px;
              color: #ffffff !important;
            }
            .content {
              padding: 24px;
              text-align: center;
            }
            .title {
              font-size: 20px;
              margin: 0 0 8px;
              font-weight: bold;
            }
            .muted {
              color: #6b7280;
              margin: 0 0 20px;
              line-height: 1.6;
            }
            .otp-box {
              display: inline-block;
              padding: 12px 24px;
              background: #f0f9ff;
              color: #1e40af;
              font-size: 28px;
              letter-spacing: 6px;
              border-radius: 12px;
              font-weight: 700;
              border: 1px solid #93c5fd;
            }
            .footer {
              padding: 16px 24px;
              border-top: 1px solid #eef2f7;
              color: #9ca3af;
              font-size: 12px;
              text-align: center;
            }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="card">
              <!-- HEADER -->
              <div class="header">
                <div class="brand">📚 BookShop • Quên mật khẩu</div>
              </div>

              <!-- CONTENT -->
              <div class="content">
                <h1 class="title">Xin chào,</h1>
                <p class="muted">
                  Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản BookShop của mình.
                </p>
                <div class="otp-box">{{otp_code}}</div>
                <p class="muted" style="margin-top:16px">
                  Mã OTP có hiệu lực trong <strong>1 phút</strong>.<br>
                  Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.
                </p>
              </div>

              <!-- FOOTER -->
              <div class="footer">
                Email này được gửi tự động, vui lòng không trả lời.<br>
                © 2025 BookShop. All rights reserved.
              </div>
            </div>
          </div>
        </body>
        </html>
        """;

            // 🔹 Thay placeholder bằng OTP thực tế
            String html = htmlTemplate.replace("{{otp_code}}", otp);

            // 🔹 Tạo và gửi mail
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(toEmail);
            helper.setSubject("Mã OTP đặt lại mật khẩu");
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }

            // Ưu tiên HTML, fallback sang plain text
            helper.setText(plainText, html);

            mailSender.send(mime);

            return otp; // 🔹 Trả về OTP để backend lưu hoặc so sánh sau này
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email OTP: " + e.getMessage(), e);
        }
    }

    public void resetPassword(Customer customer, String newPassword) {
        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
    }


    public boolean verifyUser(String verificationCode) {
        Customer customer = customerRepository.findByVerificationCode(verificationCode);;
        if (customer.isEnabled()) {
            return false;
        }
        customer.setEnabled(true);
        customer.setVerificationCode(null); // Xóa mã xác thực
        customer.setActive(true);
        customerRepository.save(customer);
        return true;
    }

    public Customer findByVerificationCode(String code) {
        return customerRepository.findByVerificationCode(code);
    }

    public Customer changePassword(Customer customer, String newPassword) {
        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        return customerRepository.save(customer);
    }

    public Customer createAccountCustomer(String username, String email, String password, String phone) {
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPasswordHash(password);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setEnabled(true);
        customer.setPasswordHash(passwordEncoder.encode(customer.getPasswordHash()));
        customer.setRole(Role.CUSTOMER);
        customer.setCreatedAt(LocalDateTime.now());
        String avatarUrl = avatarService.createAndUploadAvatar(customer.getUsername(), customer.getEmail());
        customer.setAvatarUrl(avatarUrl);
        customer.setActive(true);
        Customer savedCustomer =  customerRepository.save(customer);
        return savedCustomer;
    }


    public List<Customer> getAllCustomer() {
        return customerRepository.findAll();
    }

    public Customer updateInfoAccount(Customer customer, String username, String phone, String email) {
        customer.setUsername(username);
        customer.setPhone(phone);
        customer.setEmail(email);
        return customerRepository.save(customer);
    }

    public Long countCustomers() {
        return customerRepository.count();
    }

    public Long countCustomersBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // bao gồm cả endDate
        return customerRepository.countCustomersBetween(startDateTime, endDateTime);
    }

}
