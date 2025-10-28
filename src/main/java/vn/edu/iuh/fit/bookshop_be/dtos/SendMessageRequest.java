package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;
import vn.edu.iuh.fit.bookshop_be.models.Role;

@Data
public class SendMessageRequest {
    private String message;
    private Integer customerId;
    private Integer senderId;
    private Role senderRole;
}
