package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.models.CustomerNotification;
import vn.edu.iuh.fit.bookshop_be.models.Notification;

import java.util.List;

@Repository
public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Integer> {
    @Query("select c from CustomerNotification c where c.customer = ?1")
    List<CustomerNotification> findByCustomer(Customer customer);

    @Query("SELECT COUNT(cn) FROM CustomerNotification cn WHERE cn.customer.id = :customerId AND cn.isRead = false")
    Integer countUnreadByCustomerId(@Param("customerId") Integer customerId);

    @Query("select c from CustomerNotification c where c.notification.id = ?1")
    List<CustomerNotification> findByNotification_Id(Integer id);


}
