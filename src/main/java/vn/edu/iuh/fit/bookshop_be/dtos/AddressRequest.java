package vn.edu.iuh.fit.bookshop_be.dtos;

import lombok.Data;

@Data
public class AddressRequest {
    private String street;
    private String ward;
    private String district;
    private String city;
    private String note;
}
