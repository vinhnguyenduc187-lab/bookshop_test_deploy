package vn.edu.iuh.fit.bookshop_be.services;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.models.Cart;
import vn.edu.iuh.fit.bookshop_be.models.CartItem;
import vn.edu.iuh.fit.bookshop_be.models.Product;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.repositories.CartItemRepository;
import vn.edu.iuh.fit.bookshop_be.repositories.CartRepository;

import java.time.LocalDateTime;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public Cart addProductToCart(Customer customer, Product product, Integer quantity) {
        Cart cart = cartRepository.findByCustomer(customer);
        if (cart == null) {
           Cart newCart = new Cart();
           newCart.setUser(customer);
           newCart.setCount(0);
           newCart.setCreatedAt(LocalDateTime.now());
           cart = cartRepository.save(newCart);
        }
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product);
        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
//            cartItem.setPrice(product.getPrice());
            cartItem.setQuantity(quantity);
            cartItem.setProductName(product.getProductName());
            cart.setCount(cart.getCount() + 1);
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
        cart.setUpdatedAt(LocalDateTime.now());

        return cart;
    }

    public Cart removeProductFromCart(Customer customer, Product product) {
        Cart cart = cartRepository.findByCustomer(customer);
        if (cart != null) {
            CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product);
            if (cartItem != null) {
                cart.getItems().remove(cartItem);
                cart.setCount(cart.getCount() - 1);
                cartItemRepository.delete(cartItem);
                cartRepository.save(cart);
            }
        }
        return cart;
    }

    public Cart getCartByUser(Customer customer) {
        return cartRepository.findByCustomer(customer);
    }


}
