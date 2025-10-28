package vn.edu.iuh.fit.bookshop_be.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.dtos.NotificationRequest;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.models.CustomerNotification;
import vn.edu.iuh.fit.bookshop_be.models.Notification;
import vn.edu.iuh.fit.bookshop_be.models.Promotion;
import vn.edu.iuh.fit.bookshop_be.repositories.CustomerNotificationRepository;
import vn.edu.iuh.fit.bookshop_be.repositories.CustomerRepository;
import vn.edu.iuh.fit.bookshop_be.repositories.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final CustomerNotificationRepository customerNotificationRepository;
    private final CustomerRepository customerRepository;

    public NotificationService(NotificationRepository notificationRepository, CustomerNotificationRepository customerNotificationRepository, CustomerRepository customerRepository) {
        this.notificationRepository = notificationRepository;
        this.customerNotificationRepository = customerNotificationRepository;
        this.customerRepository = customerRepository;
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    public Notification createNotification(String title, String message) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notificationRepository.save(notification);

        // Gửi đến tất cả khách hàng
        List<Customer> customers = customerRepository.findAll();
        for (Customer customer : customers) {
            CustomerNotification cn = new CustomerNotification();
            cn.setCustomer(customer);
            cn.setNotification(notification);
            cn.setRead(false);
            customerNotificationRepository.save(cn);
        }

        // Gửi realtime qua WebSocket
        messagingTemplate.convertAndSend("/topic/notifications", notification);

        return notification;
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Integer countUnreadNotificationsForCustomer(Integer customerId) {
        return customerNotificationRepository.countUnreadByCustomerId(customerId);
    }

    // read all notification for customer
    public void readNotification(Integer customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            List<CustomerNotification> customerNotifications = customerNotificationRepository.findByCustomer(customer);
            for (CustomerNotification cn : customerNotifications) {
                cn.setRead(true);
            }
            customerNotificationRepository.saveAll(customerNotifications);
        }
    }

    public Notification findById(Integer id) {
        return notificationRepository.findById(id).orElse(null);
    }

    public Notification updateNotification(Integer id, Notification notification) {
        if (notificationRepository.existsById(id)) {
            notification.setId(id);
            return notificationRepository.save(notification);
        }
        return null;
    }

    public void deleteNotification(Integer id) {
        if (notificationRepository.existsById(id)) {
            List<CustomerNotification> customerNotifications = customerNotificationRepository.findByNotification_Id(id);
            customerNotificationRepository.deleteAll(customerNotifications);
            notificationRepository.deleteById(id);
        } else {
            throw new RuntimeException("Notification not found with id: " + id);
        }
    }
}
