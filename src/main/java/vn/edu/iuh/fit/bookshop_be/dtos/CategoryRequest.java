package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;

@Data
public class CategoryRequest {
    private String categoryName;
    private String description;
    private Integer parentId;
}
