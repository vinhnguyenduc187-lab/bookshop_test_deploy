package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;
import vn.edu.iuh.fit.bookshop_be.models.Role;

@Data
public class CreateAccountRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
    private Role role;
}
