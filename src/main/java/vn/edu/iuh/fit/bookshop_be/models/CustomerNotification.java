package vn.edu.iuh.fit.bookshop_be.models;


import jakarta.persistence.*;

@Entity
@Table(name = "customer_notifications")
public class CustomerNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Mỗi user có thể đọc hoặc chưa đọc thông báo này
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "is_read")
    private boolean isRead = false;

    public CustomerNotification() {
    }

    public CustomerNotification(Integer id, Customer customer, Notification notification, boolean isRead) {
        this.id = id;
        this.customer = customer;
        this.notification = notification;
        this.isRead = isRead;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }



    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}


