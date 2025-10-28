package vn.edu.iuh.fit.bookshop_be.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookshop_be.dtos.AddProductToCartReqest;
import vn.edu.iuh.fit.bookshop_be.models.Cart;
import vn.edu.iuh.fit.bookshop_be.models.CartItem;
import vn.edu.iuh.fit.bookshop_be.models.Product;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.services.CartService;
import vn.edu.iuh.fit.bookshop_be.services.ProductService;
import vn.edu.iuh.fit.bookshop_be.services.CustomerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final CustomerService customerService;
    private final ProductService productService;

    public CartController(CartService cartService, CustomerService customerService, ProductService productService) {
        this.cartService = cartService;
        this.customerService = customerService;
        this.productService = productService;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     * @param authHeader Header chứa token xác thực người dùng
     * @param request Chứa thông tin sản phẩm và số lượng cần thêm vào giỏ hàng
     * @return Trả về thông tin giỏ hàng sau khi thêm sản phẩm thành công
     */
    @PostMapping("/addProductToCart")
    public ResponseEntity<Map<String, Object>> createCategory(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AddProductToCartReqest request
            ) {
        Map<String, Object> response = new HashMap<>();
        Integer productId = request.getProductId();
        Integer quantity = request.getQuantity();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);

            if (customer == null ) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Product product = productService.findById(productId);
            if (product == null) {
                response.put("status", "error");
                response.put("message", "Sản phẩm không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (quantity <= 0) {
                response.put("status", "error");
                response.put("message", "Số lượng sản phẩm phải lớn hơn 0");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Cart cart = cartService.addProductToCart(customer, product, quantity);
            response.put("status", "success");
            response.put("message", "Sản phẩm đã được thêm vào giỏ hàng thành công ");
            Map<String, Object> data = new HashMap<>();
            data.put("cartId", cart.getId());
            data.put("count", cart.getCount());
            data.put("createdAt", cart.getCreatedAt());
            data.put("updatedAt", cart.getUpdatedAt());
            data.put("userId", cart.getUser().getId());
            data.put("items", cart.getItems());
            response.put("status", "success");
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi thêm sản phẩm vào giỏ hàng: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Lấy thông tin giỏ hàng của người dùng
     * @param authHeader Header chứa token xác thực người dùng
     * @return Trả về thông tin giỏ hàng của người dùng
     */
    @DeleteMapping("/removeProductFromCart")
    public ResponseEntity<Map<String, Object>> removeProductFromCart(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam Integer productId
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xóa sản phẩm khỏi giỏ hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Product product = productService.findById(productId);
            if (product == null) {
                response.put("status", "error");
                response.put("message", "Sản phẩm không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Cart cart = cartService.removeProductFromCart(customer, product);
            if (cart == null) {
                response.put("status", "error");
                response.put("message", "Giỏ hàng không tồn tại hoặc sản phẩm không có trong giỏ hàng");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("status", "success");
            response.put("message", "Sản phẩm đã được xóa khỏi giỏ hàng thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("cartId", cart.getId());
            data.put("count", cart.getCount());
            data.put("createdAt", cart.getCreatedAt());
            data.put("updatedAt", cart.getUpdatedAt());
            data.put("userId", cart.getUser().getId());
            data.put("items", cart.getItems());
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi xóa sản phẩm khỏi giỏ hàng: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Lấy thông tin giỏ hàng của người dùng
     * @param authHeader Header chứa token xác thực người dùng
     * @return Trả về thông tin giỏ hàng của người dùng
     */
    @GetMapping("/getCart")
    public ResponseEntity<Map<String, Object>> getCart(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        try {
            Customer customer = customerService.getCustomerByToken(authHeader);
            if (customer == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xem giỏ hàng");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            Cart cart = cartService.getCartByUser(customer);
            if (cart == null) {
                response.put("status", "error");
                response.put("message", "Giỏ hàng không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("status", "success");
            response.put("message", "Lấy giỏ hàng thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("cartId", cart.getId());
            data.put("count", cart.getCount());
            data.put("createdAt", cart.getCreatedAt());
            data.put("updatedAt", cart.getUpdatedAt());
            data.put("userId", cart.getUser().getId());
            // build danh sách item thủ công
            List<Map<String, Object>> items = new ArrayList<>();
            for (CartItem cartItem : cart.getItems()) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("id", cartItem.getId());
                itemData.put("productId", cartItem.getProduct().getId());
                itemData.put("productName", cartItem.getProduct().getProductName());
                itemData.put("productImage", cartItem.getProduct().getImageUrls());
                itemData.put("quantity", cartItem.getQuantity());
//                itemData.put("price", cartItem.getPrice());
                items.add(itemData);
            }
            data.put("items", items);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy giỏ hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
