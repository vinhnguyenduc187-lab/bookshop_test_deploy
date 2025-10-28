package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.StockReceipt;
import vn.edu.iuh.fit.bookshop_be.models.StockReceiptDetail;

import java.util.List;

@Repository
public interface StockReceiptDetailRepository extends JpaRepository<StockReceiptDetail, Integer> {
    @Query("select s from StockReceiptDetail s where s.stockReceipt = ?1")
    List<StockReceiptDetail> findByStockReceipt(StockReceipt stockReceipt);
}
