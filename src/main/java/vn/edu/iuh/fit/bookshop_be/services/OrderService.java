package vn.edu.iuh.fit.bookshop_be.services;

import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.dtos.ProductOrderRequest;
import vn.edu.iuh.fit.bookshop_be.dtos.ProductStockReceiptRequest;
import vn.edu.iuh.fit.bookshop_be.models.*;
import vn.edu.iuh.fit.bookshop_be.repositories.InventoryRepository;
import vn.edu.iuh.fit.bookshop_be.repositories.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final PromotionService promotionService;
    private final StockReceiptService stockReceiptService;
    private final InventoryRepository inventoryRepository;


    public OrderService(OrderRepository orderRepository, ProductService productService, PromotionService promotionService, StockReceiptService stockReceiptService, InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.promotionService = promotionService;
        this.stockReceiptService = stockReceiptService;
        this.inventoryRepository = inventoryRepository;
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Order placeOrder(Customer customer, PaymentMethod paymentMethod, String address, String phone , String note, List<ProductOrderRequest> productOrderRequests, String promotionCode) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setPaymentMethod(paymentMethod);
        order.setAddress(address);
        order.setPhone(phone);
        order.setNote(note);
        order.setCreatedAt(LocalDateTime.now());
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (ProductOrderRequest request : productOrderRequests) {
            OrderItem orderItem = new OrderItem();
            Integer productId = request.getProductId();
            Product product = productService.findById(productId);
            if (product == null) {
                throw new RuntimeException("Product not found with ID: " + productId);
            }
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setPrice(product.getPriceAfterDiscount());
            orderItem.setQuantity(request.getQuantity());
            orderItem.setProductName(product.getProductName());
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                orderItem.setProductImage(product.getImageUrls().get(0));
            } else {
                orderItem.setProductImage("https://res.cloudinary.com/dzljcagp9/image/upload/v1756805790/default_product_image_fdywaa.png");
            }
            totalAmount = totalAmount.add(product.getPriceAfterDiscount().multiply(BigDecimal.valueOf(request.getQuantity())));
            orderItems.add(orderItem);

//            productService.updateProductStock(product, request.getQuantity());
        }
        if(totalAmount.compareTo(new BigDecimal("500000")) >= 0){
            order.setShippingFee((BigDecimal.ZERO));
        } else {
            order.setShippingFee(new BigDecimal("30000"));
        }
        totalAmount = totalAmount.add(order.getShippingFee());
        if(promotionCode != null && !promotionCode.isEmpty()){
            Promotion promotion = promotionService.findByCode(promotionCode);
            if (promotion == null) {
                throw new RuntimeException("Promotion not found with code: " + promotionCode);
            }
            if(promotion.getStatus() != PromotionStatus.ACTIVE) {
                throw new RuntimeException("Promotion is not active");
            }
            order.setPromotion(promotion);
            order.setDiscountPercent(promotion.getDiscountPercent());
            BigDecimal totalAmountAfterDiscount = totalAmount.subtract(
                    totalAmount.multiply(BigDecimal.valueOf(promotion.getDiscountPercent())).divide(BigDecimal.valueOf(100))
            );
            order.setTotalAmount(totalAmountAfterDiscount);
        } else {
            order.setPromotion(null);
            order.setDiscountPercent(0.0);
            order.setTotalAmount(totalAmount);
        }
        order.setOrderItems(orderItems);
        if (paymentMethod == PaymentMethod.COD){
            order.setPaymentStatus(null);
            order.setPaymentRef(null);
            order.setStatus(OrderStatus.PENDING);
        } else {
            order.setPaymentStatus(PaymentStatus.UNPAID);
            final String uuid = UUID.randomUUID().toString().replace("-", "");
            order.setPaymentRef(uuid);
            order.setStatus(OrderStatus.UNPAID);
        }
        Order savedOrder = orderRepository.save(order);
        String orderCode = "ORD-" +
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                String.format("%06d", savedOrder.getId());

        savedOrder.setOrderCode(orderCode);
        orderRepository.save(savedOrder);

        for(ProductOrderRequest request : productOrderRequests){
            Inventory inventory = inventoryRepository.findByProduct(productService.findById(request.getProductId())).orElse(null);
            if (inventory != null) {
                inventory.setProcessingQuantity(inventory.getProcessingQuantity() + request.getQuantity());
                inventory.recalculateAvailable();
                inventoryRepository.save(inventory);
            }
        }
        return orderRepository.save(savedOrder);
    }

    public boolean checkProductQuantity(ProductOrderRequest request){
        Integer productId = request.getProductId();
        Product product = productService.findById(productId);
        if (product == null) {
            throw new RuntimeException("Product not found with ID: " + productId);
        }
        Integer requestedQuantity = request.getQuantity();
        Inventory inventory = inventoryRepository.findByProduct(product).orElse(null);
        if (inventory == null) {
            throw new RuntimeException("Inventory not found for product ID: " + productId);
        }
        if (requestedQuantity > inventory.getAvailableQuantity()) {
            return false;
        }
        return true; // Enough stock
    }

    public List<Order> findByCustomer(Customer customer) {
        return orderRepository.findByCustomer(customer, Sort.by(Sort.Direction.DESC, "createdAt"));
//        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    public Order findById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    public Order updateOrderStatus(Integer orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        if(order.getStatus() == OrderStatus.CANCELED){
            throw new RuntimeException("Cannot update status of a canceled order");
        }
        if(order.getStatus() == OrderStatus.DELIVERED){
            throw new RuntimeException("Cannot update status of a delivered order");
        }
        if(order.getStatus() == status){
            throw new RuntimeException("Order is already in status: " + status);
        }
        order.setStatus(status);
        if(status == OrderStatus.SHIPPING){
            List<ProductStockReceiptRequest> productStockReceiptRequests = new ArrayList<>();
            for(OrderItem item : order.getOrderItems()) {
                ProductStockReceiptRequest request = new ProductStockReceiptRequest();
                request.setProductId(item.getProduct().getId());
                request.setQuantity(item.getQuantity());
                productStockReceiptRequests.add(request);
            }
            stockReceiptService.save("Xuất kho cho đơn hàng : " + order.getOrderCode() ,TypeStockReceipt.EXPORT, null, "Đã vận chuyển cho đơn hàng : " + order.getOrderCode(), productStockReceiptRequests);
            for(ProductStockReceiptRequest request : productStockReceiptRequests) {
                Inventory inventory = inventoryRepository.findByProduct(productService.findById(request.getProductId())).orElse(null);
                if (inventory != null) {
                    inventory.setProcessingQuantity(inventory.getProcessingQuantity() - request.getQuantity());
                    inventory.recalculateAvailable();
                    inventoryRepository.save(inventory);
                }
            }
        }
        return orderRepository.save(order);
    }

    public Order cancelOrder(Order order, String reason) {
     updateOrderStatus(order.getId(), OrderStatus.CANCELED);
     order.setPaymentRef(null);
     order.setPaymentStatus(null);
     order.setReasonCancel(reason);
     order.setCancelledAt(LocalDateTime.now());
     List<ProductOrderRequest> productOrderRequests = new ArrayList<>();
     for(OrderItem item : order.getOrderItems()){
         ProductOrderRequest request = new ProductOrderRequest();
         request.setProductId(item.getProduct().getId());
         request.setQuantity(item.getQuantity());
         productOrderRequests.add(request);
     }
        List<ProductStockReceiptRequest> productStockReceiptRequests = new ArrayList<>();
        for(ProductOrderRequest request : productOrderRequests){
            ProductStockReceiptRequest stockRequest = new ProductStockReceiptRequest();
            stockRequest.setProductId(request.getProductId());
            stockRequest.setQuantity(request.getQuantity());
            productStockReceiptRequests.add(stockRequest);
        }
//        stockReceiptService.save(TypeStockReceipt.IMPORT, null, "Nhập kho trả lại cho đơn hàng đã hủy : " + order.getOrderCode(), productStockReceiptRequests);
     return orderRepository.save(order);
    }

    public Order findByPaymentRef(String paymentRef){
        return orderRepository.findByPaymentRef(paymentRef);
    }

    public List<Order> findAll() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Order findByIdAndUser(Integer id, Customer customer) {
        return orderRepository.findByIdAndCustomer(id, customer);
    }

    public Double calculateTotalRevenue() {
        List<Order> orders = orderRepository.findAll();
        Double totalRevenue = 0.0;
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.DELIVERED) {
                totalRevenue += order.getTotalAmount().doubleValue();
            }
        }
        return totalRevenue;
    }

    public Double calculateTotalRevenueBetween(LocalDate startDate, LocalDate endDate) {
        return orderRepository.calculateTotalRevenueBetween(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    public Long countOrdersBetween(LocalDate startDate, LocalDate endDate) {
        return orderRepository.countByOrderDateBetween(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    //countTotalProductsSold
    public Long countTotalProductSold(Integer productId) {
        Long totalSold = orderRepository.countTotalProductSold(productId);
        return totalSold != null ? totalSold : 0L;
    }

    public Long countTotalProductSoldBetween(Integer productId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay(); // bao gồm endDate
        Long totalSold = orderRepository.countTotalProductSoldBetween(productId, startDateTime, endDateTime);
        return totalSold != null ? totalSold : 0L;
    }

}
