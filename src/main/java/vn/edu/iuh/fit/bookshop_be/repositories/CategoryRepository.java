package vn.edu.iuh.fit.bookshop_be.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookshop_be.models.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("select c from Category c where c.parentCategory.slug = ?1")
    List<Category> findByParentCategory_Slug(String slug);

    @Query("select c from Category c where c.slug = ?1")
    Category findBySlug(String slug);


}
