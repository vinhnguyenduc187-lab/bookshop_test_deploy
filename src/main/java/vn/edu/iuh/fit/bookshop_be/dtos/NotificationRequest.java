package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;

@Data
public class NotificationRequest {
    private String title;
    private String message;
}
