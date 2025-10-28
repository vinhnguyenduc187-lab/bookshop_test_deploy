package vn.edu.iuh.fit.bookshop_be.models;

import jakarta.persistence.*;
import org.checkerframework.common.value.qual.MinLen;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Mỗi sản phẩm chỉ có một bản ghi tồn kho

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    // Số lượng thực tế trong kho
    @Column(name = "actual_quantity", nullable = false)
    private int actualQuantity = 0;

    // Số lượng đang xử lý (đã có đơn hàng nhưng chưa giao)
    @Column(name = "processing_quantity", nullable = false)
    private int processingQuantity = 0;

    // Số lượng còn bán được (actual - processing)
    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity = 0;

    // Thời điểm cập nhật
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // --- Hàm cập nhật lại số lượng khả dụng ---
    public void recalculateAvailable() {
        this.availableQuantity = this.actualQuantity - this.processingQuantity;
        if (this.availableQuantity < 0) this.availableQuantity = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getActualQuantity() {
        return actualQuantity;
    }


    public int getProcessingQuantity() {
        return processingQuantity;
    }


    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }


    public void setActualQuantity(int actualQuantity) {
        if (actualQuantity < 0) throw new IllegalArgumentException("Số lượng thực tế không được nhỏ hơn 0");
        this.actualQuantity = actualQuantity;
        recalculateAvailable();
    }

    public void setProcessingQuantity(int processingQuantity) {
        if (processingQuantity < 0) throw new IllegalArgumentException("Số lượng đang xử lý không được nhỏ hơn 0");
        this.processingQuantity = processingQuantity;
        recalculateAvailable();
    }

    public void setAvailableQuantity(int availableQuantity) {
        if (availableQuantity < 0) throw new IllegalArgumentException("Số lượng khả dụng không được nhỏ hơn 0");
        this.availableQuantity = availableQuantity;
    }

}

