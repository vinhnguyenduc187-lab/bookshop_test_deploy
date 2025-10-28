package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class ProductRequest {
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer discountPercentage;
    private Integer stockQuantity;
    private String packageDimensions;
    private Integer weightGrams;
    private String productType;
    private String supplierName;
    private Integer categoryId;
    private String publisherName;
    private Set<String> authors;
    private String publicationYear;
    private Integer pageCount;
    private String isbn;
    private String coverType;
    private String gradeLevel;
    private String ageRating;
    private String genres;
    private String color;
    private String material;
    private String manufacturingLocation;
    private List<String> imageUrls;
}

