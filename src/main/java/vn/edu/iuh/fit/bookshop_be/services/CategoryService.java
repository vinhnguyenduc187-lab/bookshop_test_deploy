package vn.edu.iuh.fit.bookshop_be.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.models.Category;
import vn.edu.iuh.fit.bookshop_be.repositories.CategoryRepository;

import java.util.List;

@Service
public class CategoryService{
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {

        return categoryRepository.findAll();
    }

    public List<Category> getRootCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getParentCategory() == null)
                .toList();
    }

    public Category getCategoryById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(Integer id, Category category) {
        if (categoryRepository.existsById(id)) {
            category.setId(id);
            return categoryRepository.save(category);
        }
        return null; // or throw an exception
    }

    @Transactional
    public void deleteCategoryWithSubCategories(Integer id) {
        Category category = getCategoryById(id);
        if (category != null) {
            category.getSubCategories().forEach(sub -> deleteCategoryWithSubCategories(sub.getId()));
            categoryRepository.delete(category);
        }
    }

    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public List<Category> searchCategories(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return getAllCategories();
        }
        return categoryRepository.findAll().stream()
                .filter(category -> category.getCategoryName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }

//    public List<Category> getCategoriesByProductId(Integer productId) {
//        return categoryRepository.findAll().stream()
//                .filter(category -> category.getProducts().stream()
//                        .anyMatch(product -> product.getId().equals(productId)))
//                .toList();
//    }

    public List<Category> getCategoriesByName(String name) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getCategoryName().equalsIgnoreCase(name))
                .toList();
    }

    public  List<Category> buildSubCategories(List<Category> subCategories) {
        if (subCategories == null || subCategories.isEmpty()) {
            return null;
        }
        return subCategories.stream()
                .map(subCategory -> {
                    Category categoryRender = new Category();
                    categoryRender.setId(subCategory.getId());
                    categoryRender.setCategoryName(subCategory.getCategoryName());
                    categoryRender.setDescription(subCategory.getDescription());
                    categoryRender.setSubCategories(buildSubCategories(subCategory.getSubCategories()));
                    return categoryRender;
                }).toList();
    }

    public List<Category> getCategoriesByParentSlug(String slug) {
        return categoryRepository.findByParentCategory_Slug(slug);
    }

    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    public List<Category> getSubCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getParentCategory() != null)
                .toList();
    }

}
