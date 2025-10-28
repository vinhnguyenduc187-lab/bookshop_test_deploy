package vn.edu.iuh.fit.bookshop_be.services;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.models.Category;
import vn.edu.iuh.fit.bookshop_be.models.Inventory;
import vn.edu.iuh.fit.bookshop_be.models.Product;
import vn.edu.iuh.fit.bookshop_be.repositories.InventoryRepository;
import vn.edu.iuh.fit.bookshop_be.repositories.ProductRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product findById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    public Product save(Product product) {
        Product savedProduct = productRepository.save(product);
        Optional<Inventory> existingInventory = inventoryRepository.findByProduct(savedProduct);

        Inventory inventory;
        if (existingInventory.isPresent()) {
            inventory = existingInventory.get();
            inventory.setUpdatedAt(LocalDateTime.now());
        } else {
            inventory = new Inventory();
            inventory.setProduct(savedProduct);
            inventory.setActualQuantity(0);
            inventory.setProcessingQuantity(0);
            inventory.setAvailableQuantity(0);
            inventory.setUpdatedAt(LocalDateTime.now());
        }

        inventoryRepository.save(inventory);
        return savedProduct;
    }

    public Product updateProduct(Integer id, Product product) {
        if (productRepository.existsById(id)) {
            product.setId(id);
            return productRepository.save(product);
        }
        return null; // or throw an exception
    }

    public void deleteProduct(Integer id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        }
    }

    public List<Product> findByCategory(Category category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> findByProductType(String productType) {
        return productRepository.findByProductType(productType);
    }

//    public Product updateProductStock(Product product, Integer quantity) {
//        if (product != null) {
//            product.setStockQuantity(product.getStockQuantity() - quantity);
//            return productRepository.save(product);
//        }
//        return null; // or throw an exception
//    }

//    public List<Product> findByCategoryName(String keyword) {
//        return productRepository.findByCategory_CategoryName(keyword);
//    }

    public List<Product> findByParentCategoryId(Integer id) {
        return productRepository.findByCategory_ParentCategory_Id(id);
    }

    public List<Product> findByCategoryName(String parentName, String categoryName) {
        return productRepository.findByCategory_ParentCategory_CategoryNameAndCategory_CategoryName(parentName, categoryName);
    }

    public Long countProducts() {
        return productRepository.count();
    }

    public List<Product> findByProductNameLike(String productName) {
        return productRepository.findByProductNameLike("%" + productName + "%");
    }







}
