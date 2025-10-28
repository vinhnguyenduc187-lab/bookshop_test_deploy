package vn.edu.iuh.fit.bookshop_be.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "is_sent_by_customer", nullable = false)
    private boolean isSentByCustomer;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Message() {}

    public Message(Integer id, Conversation conversation, Employee employee, String message, boolean isRead, boolean isSentByCustomer, LocalDateTime createdAt) {
        this.id = id;
        this.conversation = conversation;
        this.employee = employee;
        this.message = message;
        this.isRead = isRead;
        this.isSentByCustomer = isSentByCustomer;
        this.createdAt = createdAt;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isSentByCustomer() {
        return isSentByCustomer;
    }

    public void setSentByCustomer(boolean sentByCustomer) {
        isSentByCustomer = sentByCustomer;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
