package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;
import vn.edu.iuh.fit.bookshop_be.models.Role;

@Data
public class UpdateInfoRequest {
    private String username;
    private String phone;
    private String email;
    private Role role;
}
