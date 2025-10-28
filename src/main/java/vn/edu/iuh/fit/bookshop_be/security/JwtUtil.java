package vn.edu.iuh.fit.bookshop_be.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private SecretKey secret;
    private final long accessTokenExp = 7 * 24 * 60 * 60 * 1000;  // 7 ngày
    private final long refreshTokenExp = 7 * 24 * 60 * 60 * 1000; // 7 ngày

    @PostConstruct
    public void init() {
        if (SECRET_KEY == null || SECRET_KEY.isBlank()) {
            // Nếu không cấu hình, tự generate random key (restart app sẽ đổi key)
            secret = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        } else {
            // Sử dụng chuỗi plain text -> convert sang SecretKey
            secret = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        }
    }

    // ================= TOKEN CREATE =================

    // Tạo Access Token
    public String generateAccessToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // thêm role
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExp))
                .signWith(secret, SignatureAlgorithm.HS512)   // ✅ dùng SecretKey
                .compact();
    }

    // Tạo Refresh Token
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExp))
                .signWith(secret, SignatureAlgorithm.HS512)
                .compact();
    }

    // ================= TOKEN EXTRACT =================

    // Trích xuất email (subject) từ token
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Trích xuất role từ token
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ================= TOKEN VALIDATE =================

    public boolean validateToken(String token, String email) {
        try {
            String tokenEmail = extractEmail(token);
            return (tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
