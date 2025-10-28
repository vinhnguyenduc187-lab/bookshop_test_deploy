package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.StockReceipt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockReceiptRepository extends JpaRepository<StockReceipt, Integer> {
    @Query("SELECT s FROM StockReceipt s " +
            "WHERE s.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY s.createdAt DESC")
    List<StockReceipt> getStockReceiptsDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}
