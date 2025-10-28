package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
