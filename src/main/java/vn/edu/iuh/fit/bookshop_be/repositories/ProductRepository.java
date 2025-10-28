package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.Category;
import vn.edu.iuh.fit.bookshop_be.models.Product;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory(Category category);
    List<Product> findByProductType(String productType);

    @Query("select p from Product p where p.category.parentCategory.id = ?1")
    List<Product> findByCategory_ParentCategory_Id(Integer id);

    @Query("select p from Product p where p.category.id = ?1")
    List<Product> findByCategory_Id(Integer id);

    @Query("SELECT p FROM Product p " +
            "WHERE CONCAT('/', p.category.parentCategory.slug, '/', p.category.slug) = ?1")
    List<Product> findByFullSlug(String fullSlug);

    @Query("select p from Product p where p.category.parentCategory.slug = ?1 and p.category.slug = ?2")
    List<Product> findByCategory_ParentCategory_CategoryNameAndCategory_CategoryName(String parentSlug, String slug);

    @Query("select p from Product p where p.productName like ?1")
    List<Product> findByProductNameLike(String productName);


}
