package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ReviewProductRequest {
    private Integer orderItemId;
    private Integer rating;
    private String comment;
    private List<MultipartFile> mediaFiles;
}
