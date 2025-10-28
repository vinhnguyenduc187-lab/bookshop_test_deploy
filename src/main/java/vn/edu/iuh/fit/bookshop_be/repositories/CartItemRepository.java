package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.Cart;
import vn.edu.iuh.fit.bookshop_be.models.CartItem;
import vn.edu.iuh.fit.bookshop_be.models.Product;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    CartItem findByCartAndProduct(Cart cart, Product product);
}
