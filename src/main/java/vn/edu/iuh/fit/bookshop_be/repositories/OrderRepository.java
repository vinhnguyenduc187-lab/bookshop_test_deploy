package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.Order;
import vn.edu.iuh.fit.bookshop_be.models.Customer;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("select o from Order o where o.customer = ?1")
    List<Order> findByCustomer(Customer customer, Sort createdAt);

    @Query("select o from Order o where o.paymentRef = ?1")
    Order findByPaymentRef(String paymentRef);


    @Query("select o from Order o where o.id = ?1 and o.customer = ?2")
    Order findByIdAndCustomer(Integer id, Customer customer);

    // tinh so luong ban cua tung san pham

    @Query("SELECT SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "WHERE oi.product.id = ?1")
    Long countTotalProductSold(Integer productId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Long countByOrderDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenueBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(oi.quantity) " +
            "FROM OrderItem oi " +
            "WHERE oi.product.id = :productId " +
            "AND oi.order.createdAt BETWEEN :startDate AND :endDate")
    Long countTotalProductSoldBetween(@Param("productId") Integer productId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);



}
