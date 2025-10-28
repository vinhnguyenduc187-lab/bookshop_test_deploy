package vn.edu.iuh.fit.bookshop_be.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_receipt")
public class StockReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = true)
    @JsonIgnore
    private Employee employee;

    @Column(name = "name_stock_receipt")
    private String nameStockReceipt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_stock_receipt")
    private TypeStockReceipt typeStockReceipt; // IMPORT hoặc EXPORT

    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();


    @OneToMany(mappedBy = "stockReceipt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<StockReceiptDetail> details = new ArrayList<>();


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TypeStockReceipt getTypeStockReceipt() {
        return typeStockReceipt;
    }

    public void setTypeStockReceipt(TypeStockReceipt typeStockReceipt) {
        this.typeStockReceipt = typeStockReceipt;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<StockReceiptDetail> getDetails() { return details; }
    public void setDetails(List<StockReceiptDetail> details) { this.details = details; }

    public String getNameStockReceipt() {
        return nameStockReceipt;
    }

    public void setNameStockReceipt(String nameStockReceipt) {
        this.nameStockReceipt = nameStockReceipt;
    }
}

