package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.Inventory;
import vn.edu.iuh.fit.bookshop_be.models.Product;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    @Query("select i from Inventory i where i.product = ?1")
    Optional<Inventory> findByProduct(Product product);
}
