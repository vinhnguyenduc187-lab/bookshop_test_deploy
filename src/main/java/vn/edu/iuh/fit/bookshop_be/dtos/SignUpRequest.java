package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;

@Data
public class SignUpRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
}
