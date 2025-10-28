package vn.edu.iuh.fit.bookshop_be.services;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.models.OrderItem;
import vn.edu.iuh.fit.bookshop_be.models.Product;
import vn.edu.iuh.fit.bookshop_be.models.ProductReview;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.repositories.ProductReviewRepository;

import java.util.List;

@Service
public class ProductReviewService {
    private final ProductReviewRepository productReviewRepository;

    public ProductReviewService(ProductReviewRepository productReviewRepository) {
        this.productReviewRepository = productReviewRepository;
    }

    public ProductReview save(ProductReview productReview) {
        return productReviewRepository.save(productReview);
    }

    public ProductReview createProductReview(OrderItem orderItem, Integer rating, String comment, Customer customer, Product product) {
        ProductReview productReview = new ProductReview();
        productReview.setOrderItem(orderItem);
        productReview.setRating(rating);
        productReview.setComment(comment);
        productReview.setUser(customer);
        productReview.setProduct(product);
        productReview.setUserName(customer.getUsername());
        productReview.setReviewDate(java.time.LocalDateTime.now());
        orderItem.setReviewed(true);
        return productReviewRepository.save(productReview);
    }

    public List<ProductReview> getReviewsByProduct(Product product) {
        return productReviewRepository.findByProduct(product);
    }

}
