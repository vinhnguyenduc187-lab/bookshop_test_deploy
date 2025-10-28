package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.Promotion;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    @Query("select p from Promotion p where p.code = ?1")
    Promotion findByCode(String code);
}
