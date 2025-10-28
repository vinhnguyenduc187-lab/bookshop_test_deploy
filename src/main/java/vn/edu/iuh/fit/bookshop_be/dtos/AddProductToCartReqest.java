package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;

@Data
public class AddProductToCartReqest {
    private Integer productId;
    private Integer quantity;
}
