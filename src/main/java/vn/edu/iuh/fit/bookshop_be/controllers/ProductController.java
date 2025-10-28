package vn.edu.iuh.fit.bookshop_be.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.bookshop_be.dtos.ProductRequest;
import vn.edu.iuh.fit.bookshop_be.models.*;
import vn.edu.iuh.fit.bookshop_be.services.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;
    private final CategoryService categoryService;
    private final Cloudinary cloudinary;
    private final InventoryService inventoryService;


    public ProductController(ProductService productService, CustomerService customerService, EmployeeService employeeService, CategoryService categoryService, Cloudinary cloudinary, InventoryService inventoryService) {
        this.productService = productService;
        this.customerService = customerService;
        this.employeeService = employeeService;
        this.categoryService = categoryService;
        this.cloudinary = cloudinary;
        this.inventoryService = inventoryService;
    }

    /**
     * Tạo sản phẩm mới
     * @param authHeader
     * @param request
     * @param images
     * @return ResponseEntity với thông tin về sản phẩm đã tạo hoặc lỗi nếu có
     */
    @PostMapping("/createProduct")
    public ResponseEntity<Map<String, Object>> createCategory(
            @RequestHeader("Authorization") String authHeader,
            @RequestPart("request") ProductRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ){
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);

            if (employee == null ) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để tạo sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            if (employee.getRole() == null || ( employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền tạo sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Product product = new Product();

            //Gán các thuộc tính từ request vào product
            product.setProductName(request.getProductName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setDiscountPercentage(0); // Mặc định không có giảm giá
            product.setPriceAfterDiscount(request.getPrice()); // Giá sau giảm giá ban đầu bằng giá gốc
//            product.setStockQuantity(0);
            product.setPackageDimensions(request.getPackageDimensions());
            product.setWeightGrams(request.getWeightGrams());
            product.setProductType(request.getProductType());
            product.setSupplierName(request.getSupplierName());


            // Kiểm tra và gán danh mục
            Category category = categoryService.findById(request.getCategoryId());
            if (category == null) {
                response.put("status", "error");
                response.put("message", "Danh mục không tồn tại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            product.setCategory(category);

            product.setPublisherName(request.getPublisherName());
            product.setAuthorNames(request.getAuthors() != null ? request.getAuthors() : new HashSet<>());
            product.setPublicationYear(request.getPublicationYear());
            product.setPageCount(request.getPageCount());
            product.setCoverType(request.getCoverType());
            product.setGradeLevel(request.getGradeLevel());
            product.setAgeRating(request.getAgeRating());
            product.setGenres(request.getGenres());
            product.setColor(request.getColor());
            product.setMaterial(request.getMaterial());
            product.setManufacturingLocation(request.getManufacturingLocation());

            // Lưu sản phẩm
            Product savedProduct = productService.save(product);

            // Xử lý upload nhiều hình ảnh
            List<String> imageUrls = new ArrayList<>();
            if (images != null) {
                for (MultipartFile image : images) {
                    if (image != null && !image.isEmpty()) {
                        try {
                            String folderPath = "products/"  + savedProduct.getId() + "-" + savedProduct.getProductName();
                            Map uploadResult = cloudinary.uploader().upload(image.getBytes(),
                                    ObjectUtils.asMap("folder", folderPath));

                            String imageUrl = (String) uploadResult.get("secure_url");
                            imageUrls.add(imageUrl);
                        } catch (IOException e) {
                            response.put("status", "error");
                            response.put("message", "Lỗi khi tải ảnh lên Cloudinary: " + e.getMessage());
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                        }
                    }
                }
            }
            if(imageUrls == null || imageUrls.isEmpty()) {
                // Nếu không có ảnh nào được tải lên, sử dụng ảnh mặc định
                imageUrls.add("https://res.cloudinary.com/dzljcagp9/image/upload/v1756805790/default_product_image_fdywaa.png");
            }
            savedProduct.setImageUrls(imageUrls);
            savedProduct = productService.save(savedProduct);

            response.put("status", "success");
            response.put("message", "Tạo sản phẩm thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("product", savedProduct);
            response.put("data", data);


            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi tạo sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách tất cả sản phẩm
     * @return ResponseEntity với danh sách sản phẩm hoặc lỗi nếu có
     */
    @GetMapping("/getAllProducts")
    public ResponseEntity<Map<String, Object>> getAllProduct(
    ) {
        Map<String, Object> response = new HashMap<>();
        try{

            List<Product> products = productService.getAllProducts();
            if (products.isEmpty()) {
                response.put("status", "success");
                response.put("message", "Không có sản phẩm nào");
                response.put("data", Collections.emptyList());
                return ResponseEntity.ok(response);
            }
            response.put("status", "success");
            response.put("message", "Lấy danh sách sản phẩm thành công");
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> productData = new HashMap<>();
                Inventory inventory = inventoryService.findByProduct(product).getInventory();
                productData.put("id", product.getId());
                productData.put("productName", product.getProductName());
                productData.put("description", product.getDescription());
                productData.put("price", product.getPrice());
                productData.put("stockQuantity", inventory.getActualQuantity());
                productData.put("processingQuantity", inventory.getProcessingQuantity());
                productData.put("availableQuantity", inventory.getAvailableQuantity());
                productData.put("packageDimensions", product.getPackageDimensions());
                productData.put("weightGrams", product.getWeightGrams());
                productData.put("productType", product.getProductType());
                productData.put("supplierName", product.getSupplierName());
                productData.put("discountPercentage", product.getDiscountPercentage());
                productData.put("priceAfterDiscount", product.getPriceAfterDiscount());
                productData.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
                productData.put("publisherName", product.getPublisherName());
                productData.put("authorNames", product.getAuthorNames());
                productData.put("publicationYear", product.getPublicationYear());
                productData.put("pageCount", product.getPageCount());
                productData.put("coverType", product.getCoverType());
                productData.put("gradeLevel", product.getGradeLevel());
                productData.put("ageRating", product.getAgeRating());
                productData.put("genres", product.getGenres());
                productData.put("color", 	product.getColor());
                productData.put("material", 	product.getMaterial());
                productData.put("manufacturingLocation", 	product.getManufacturingLocation());
                // Chuyển đổi danh sách ảnh thành danh sách URL
                List<String> imageUrls = new ArrayList<>(product.getImageUrls());
                productData.put("imageUrls", imageUrls);

                // Thêm vào danh sách sản phẩm
                productList.add(productData);
                Map<String, Object> data = new HashMap<>();
                data.put("products", productList);
                response.put("data", data);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        }
    }

    @DeleteMapping("/deleteProduct/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xóa sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (employee.getRole() == null || ( employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền xóa sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            Product product = productService.findById(id);
            if (product == null) {
                response.put("status", "error");
                response.put("message", "Sản phẩm không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            productService.deleteProduct(id);
            response.put("status", "success");
            response.put("message", "Xóa sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi xóa sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật thông tin sản phẩm
     * @param authHeader
     * @param id
     * @param request
     * @return ResponseEntity với thông tin về sản phẩm đã cập nhật hoặc lỗi nếu có
     */
    @PutMapping("/updateProduct/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id,
            @RequestBody ProductRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để cập nhật sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (employee.getRole() == null || ( employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền cập nhật sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            Product product = productService.findById(id);
            if (product == null) {
                response.put("status", "error");
                response.put("message", "Sản phẩm không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Cập nhật các thuộc tính từ request
            product.setProductName(request.getProductName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
//            product.setStockQuantity(request.getStockQuantity());
            product.setPackageDimensions(request.getPackageDimensions());
            product.setWeightGrams(request.getWeightGrams());
            product.setProductType(request.getProductType());

            // Kiểm tra và gán danh mục
            Category category = categoryService.findById(request.getCategoryId());
            if (category == null) {
                response.put("status", "error");
                response.put("message", "Danh mục không tồn tại");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            product.setCategory(category);

            product.setPublisherName(request.getPublisherName());
            product.setAuthorNames(request.getAuthors() != null ? request.getAuthors() : new HashSet<>());
            product.setPublicationYear(request.getPublicationYear());
            product.setPageCount(request.getPageCount());
            product.setCoverType(request.getCoverType());
            product.setGradeLevel(request.getGradeLevel());
            product.setAgeRating(request.getAgeRating());
            product.setGenres(request.getGenres());
            product.setColor(request.getColor());
            product.setMaterial(request.getMaterial());
            product.setManufacturingLocation(request.getManufacturingLocation());
            // Cập nhật sản phẩm
            Product updatedProduct = productService.updateProduct(id, product);
            if (updatedProduct == null) {
                response.put("status", "error");
                response.put("message", "Cập nhật sản phẩm không thành công");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            response.put("status", "success");
            response.put("message", "Cập nhật sản phẩm thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("product", updatedProduct);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin sản phẩm theo ID
     * @param id
     * @return ResponseEntity với thông tin sản phẩm hoặc lỗi nếu có
     */
    @GetMapping("/getProductById/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(
            @PathVariable Integer id
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.findById(id);
            if (product == null) {
                response.put("status", "error");
                response.put("message", "Sản phẩm không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("status", "success");
            response.put("message", "Lấy thông tin sản phẩm thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("product", product);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy thông tin sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách sản phẩm theo danh mục
     * @param categoryId
     * @return ResponseEntity với danh sách sản phẩm hoặc lỗi nếu có
     */
    @GetMapping("/getProductsByCategory/{categoryId}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(
            @PathVariable Integer categoryId
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Category category = categoryService.findById(categoryId);
            if (category == null) {
                response.put("status", "error");
                response.put("message", "Danh mục không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            List<Product> products = productService.findByCategory(category);
            if (products.isEmpty()) {
                response.put("status", "success");
                response.put("message", "Không có sản phẩm nào trong danh mục này");
                response.put("data", Collections.emptyList());
                return ResponseEntity.ok(response);
            }
            response.put("status", "success");
            response.put("message", "Lấy danh sách sản phẩm theo danh mục thành công");
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("id", product.getId());
                productData.put("productName", product.getProductName());
                productData.put("description", product.getDescription());
                productData.put("price", product.getPrice());
//                productData.put("stockQuantity", product.getStockQuantity());
                productData.put("packageDimensions", product.getPackageDimensions());
                productData.put("weightGrams", product.getWeightGrams());
                productData.put("productType", product.getProductType());
                productData.put("categoryId", categoryId);
                productData.put("supplierName", product.getSupplierName());
                productData.put("discountPercentage", product.getDiscountPercentage());
                productData.put("priceAfterDiscount", product.getPriceAfterDiscount());
                productData.put("publisherName", product.getPublisherName());
                productData.put("authorNames", product.getAuthorNames());
                productData.put("publicationYear", product.getPublicationYear());
                productData.put("pageCount", product.getPageCount());
                productData.put("coverType", product.getCoverType());
                productData.put("gradeLevel", product.getGradeLevel());
                productData.put("ageRating", product.getAgeRating());
                productData.put("genres", product.getGenres());
                // Chuyển đổi danh sách ảnh thành danh sách URL
                List<String> imageUrls = new ArrayList<>(product.getImageUrls());
                productData.put("imageUrls", imageUrls);
                productData.put("color", product.getColor());
                productData.put("material", product.getMaterial());
                productData.put("manufacturingLocation", product.getManufacturingLocation());
                // Thêm vào danh sách sản phẩm
                productList.add(productData);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("products", productList);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách sản phẩm theo danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách sản phẩm theo loại sản phẩm
     * @param productName
     * @return ResponseEntity với danh sách sản phẩm hoặc lỗi nếu có
     */
    @GetMapping("/getProductsByProductType/{productName}")
    public ResponseEntity<Map<String, Object>> getProductsByProductType(
            @PathVariable String productName
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> products = productService.findByProductType(productName);
            if (products.isEmpty()) {
                response.put("status", "success");
                response.put("message", "Không có sản phẩm nào với loại sản phẩm này");
                response.put("data", Collections.emptyList());
                return ResponseEntity.ok(response);
            }
            response.put("status", "success");
            response.put("message", "Lấy danh sách sản phẩm theo loại sản phẩm thành công");
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("id", product.getId());
                productData.put("productName", product.getProductName());
                productData.put("description", product.getDescription());
                productData.put("price", product.getPrice());
//                productData.put("stockQuantity", product.getStockQuantity());
                productData.put("packageDimensions", product.getPackageDimensions());
                productData.put("weightGrams", product.getWeightGrams());
                productData.put("productType", product.getProductType());
                productData.put("supplierName", product.getSupplierName());
                productData.put("discountPercentage", product.getDiscountPercentage());
                productData.put("priceAfterDiscount", product.getPriceAfterDiscount());
                // Chuyển đổi danh sách ảnh thành danh sách URL
                List<String> imageUrls = new ArrayList<>(product.getImageUrls());
                productData.put("imageUrls", imageUrls);
                // Thêm vào danh sách sản phẩm
                productList.add(productData);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("products", productList);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách sản phẩm theo loại sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa ảnh khỏi sản phẩm
     * @param productId
     * @return ResponseEntity với thông tin về việc xóa ảnh hoặc lỗi nếu có
     */
    @DeleteMapping("/deleteImage/{productId}")
    public ResponseEntity<Map<String, Object>> deleteImageFromProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer productId,
            @RequestParam String imageUrl
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xóa ảnh khỏi sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (employee.getRole() == null || ( employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền xóa ảnh khỏi sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            Product product = productService.findById(productId);
            if (product == null) {
                response.put("status", "error");
                response.put("message", "Sản phẩm không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            List<String> imageUrls = new ArrayList<>(product.getImageUrls());
            if (!imageUrls.remove(imageUrl)) {
                response.put("status", "error");
                response.put("message", "Ảnh không tồn tại trong sản phẩm");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            product.setImageUrls(imageUrls);
            productService.save(product);
            response.put("status", "success");
            response.put("message", "Xóa ảnh khỏi sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi xóa ảnh khỏi sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Thêm một hoặc nhiều ảnh vào sản phẩm
     * @param productId
     * @return ResponseEntity với thông tin về việc thêm ảnh hoặc lỗi nếu có
     */
    @PostMapping("/addImages/{productId}")
    public ResponseEntity<Map<String, Object>> addImageToProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer productId,
            @RequestPart("images") MultipartFile[] images
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để thêm ảnh vào sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (employee.getRole() == null || ( employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền thêm ảnh vào sản phẩm");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            Product product = productService.findById(productId);
            if (product == null) {
                response.put("status", "error");
                response.put("message", "Sản phẩm không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            List<String> imageUrls = new ArrayList<>(product.getImageUrls());
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    try {
                        Map uploadResult = cloudinary.uploader().upload(image.getBytes(),
                                ObjectUtils.asMap("folder", "products"));
                        String imageUrl = (String) uploadResult.get("secure_url");
                        imageUrls.add(imageUrl);
                    } catch (IOException e) {
                        response.put("status", "error");
                        response.put("message", "Lỗi khi tải ảnh lên Cloudinary: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                    }
                }
            }
            product.setImageUrls(imageUrls);
            productService.save(product);
            response.put("status", "success");
            response.put("message", "Thêm ảnh vào sản phẩm thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("product", product);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi thêm ảnh vào sản phẩm: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

//    @GetMapping("/getByCategoryName")
//    public ResponseEntity<Map<String, Object>> getProductsByCategoryName(
//            @RequestParam Map<> categoryName
//    ) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<Product> products = productService.findByCategoryName(categoryName.trim());
//            if (products.isEmpty()) {
//                response.put("status", "success");
//                response.put("message", "Không có sản phẩm nào trong danh mục này");
//                response.put("data", Collections.emptyList());
//                return ResponseEntity.ok(response);
//            }
//
//            response.put("status", "success");
//            response.put("message", "Lấy danh sách sản phẩm theo danh mục thành công");
//
//            List<Map<String, Object>> productList = new ArrayList<>();
//            for (Product product : products) {
//                Map<String, Object> productData = new HashMap<>();
//                productData.put("id", product.getId());
//                productData.put("productName", product.getProductName());
//                productData.put("description", product.getDescription());
//                productData.put("price", product.getPrice());
//                productData.put("stockQuantity", product.getStockQuantity());
//                productData.put("packageDimensions", product.getPackageDimensions());
//                productData.put("weightGrams", product.getWeightGrams());
//                productData.put("productType", product.getProductType());
//                productData.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
//                productData.put("publisherName", product.getPublisherName());
//                productData.put("authorNames", product.getAuthorNames());
//                productData.put("publicationYear", product.getPublicationYear());
//                productData.put("pageCount", product.getPageCount());
//                productData.put("isbn", product.getIsbn());
//                productData.put("coverType", product.getCoverType());
//                productData.put("gradeLevel", product.getGradeLevel());
//                productData.put("ageRating", product.getAgeRating());
//                productData.put("genres", product.getGenres());
//                productData.put("imageUrls", new ArrayList<>(product.getImageUrls()));
//                productList.add(productData);
//            }
//
//            Map<String, Object> data = new HashMap<>();
//            data.put("products", productList);
//            response.put("data", data);
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("status", "error");
//            response.put("message", "Lỗi khi lấy danh sách sản phẩm theo danh mục: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }

    @GetMapping("/getByParentCategoryId/{id}")
    public ResponseEntity<Map<String, Object>> getProductsByParentCategoryId(
            @PathVariable Integer id
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> products = productService.findByParentCategoryId(id);
            if (products.isEmpty()) {
                response.put("status", "success");
                response.put("message", "Không có sản phẩm nào trong danh mục này");
                response.put("data", Collections.emptyList());
                return ResponseEntity.ok(response);
            }
            response.put("status", "success");
            response.put("message", "Lấy danh sách sản phẩm theo danh mục thành công");
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("id", product.getId());
                productData.put("productName", product.getProductName());
                productData.put("description", product.getDescription());
                productData.put("price", product.getPrice());
//                productData.put("stockQuantity", product.getStockQuantity());
                productData.put("packageDimensions", product.getPackageDimensions());
                productData.put("weightGrams", product.getWeightGrams());
                productData.put("productType", product.getProductType());
                productData.put("supplierName", product.getSupplierName());
                productData.put("discountPercentage", product.getDiscountPercentage());
                productData.put("priceAfterDiscount", product.getPriceAfterDiscount());
                productData.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
                productData.put("publisherName", product.getPublisherName());
                productData.put("authorNames", product.getAuthorNames());
                productData.put("publicationYear", product.getPublicationYear());
                productData.put("pageCount", product.getPageCount());
                productData.put("coverType", product.getCoverType());
                productData.put("gradeLevel", product.getGradeLevel());
                productData.put("ageRating", product.getAgeRating());
                productData.put("genres", product.getGenres());
                // Chuyển đổi danh sách ảnh thành danh sách URL
                List<String> imageUrls = new ArrayList<>(product.getImageUrls());
                productData.put("imageUrls", imageUrls);
                // Thêm vào danh sách sản phẩm
                productList.add(productData);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("products", productList);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách sản phẩm theo danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

//    @GetMapping("/getByCategoryId/{id}")
//    public ResponseEntity<Map<String, Object>> getProductsByCategoryId(
//            @PathVariable Integer id
//    ) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<Product> products = productService.findByCategoryId(id);
//            if (products.isEmpty()) {
//                response.put("status", "success");
//                response.put("message", "Không có sản phẩm nào trong danh mục này");
//                response.put("data", Collections.emptyList());
//                return ResponseEntity.ok(response);
//            }
//            response.put("status", "success");
//            response.put("message", "Lấy danh sách sản phẩm theo danh mục thành công");
//            List<Map<String, Object>> productList = new ArrayList<>();
//            for (Product product : products) {
//                Map<String, Object> productData = new HashMap<>();
//                productData.put("id", product.getId());
//                productData.put("productName", product.getProductName());
//                productData.put("description", product.getDescription());
//                productData.put("price", product.getPrice());
//                productData.put("stockQuantity", product.getStockQuantity());
//                productData.put("packageDimensions", product.getPackageDimensions());
//                productData.put("weightGrams", product.getWeightGrams());
//                productData.put("productType", product.getProductType());
//                productData.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
//                productData.put("publisherName", product.getPublisherName());
//                productData.put("authorNames", product.getAuthorNames());
//                productData.put("publicationYear", product.getPublicationYear());
//                productData.put("pageCount", product.getPageCount());
//                productData.put("isbn", product.getIsbn());
//                productData.put("coverType", product.getCoverType());
//                productData.put("gradeLevel", product.getGradeLevel());
//                productData.put("ageRating", product.getAgeRating());
//                productData.put("genres", product.getGenres());
//                // Chuyển đổi danh sách ảnh thành danh sách URL
//                List<String> imageUrls = new ArrayList<>(product.getImageUrls());
//                productData.put("imageUrls", imageUrls);
//                // Thêm vào danh sách sản phẩm
//                productList.add(productData);
//            }
//            Map<String, Object> data = new HashMap<>();
//            data.put("products", productList);
//            response.put("data", data);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            response.put("status", "error");
//            response.put("message", "Lỗi khi lấy danh sách sản phẩm theo danh mục: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }

    @GetMapping("/{categoryParentName}/{categoryName}")
    public ResponseEntity<Map<String, Object>> getProductsByCategoryName(
            @PathVariable String categoryParentName,
            @PathVariable String categoryName

    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> products = productService.findByCategoryName(categoryParentName,categoryName);
            if (products.isEmpty()) {
                response.put("status", "success");
                response.put("message", "Không có sản phẩm nào trong danh mục này");
                response.put("data", Collections.emptyList());
                return ResponseEntity.ok(response);
            }
            response.put("status", "success");
            response.put("message", "Lấy danh sách sản phẩm theo danh mục thành công");
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("id", product.getId());
                productData.put("productName", product.getProductName());
                productData.put("description", product.getDescription());
                productData.put("price", product.getPrice());
//                productData.put("stockQuantity", product.getStockQuantity());
                productData.put("packageDimensions", product.getPackageDimensions());
                productData.put("weightGrams", product.getWeightGrams());
                productData.put("productType", product.getProductType());
                productData.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
                productData.put("publisherName", product.getPublisherName());
                productData.put("supplierName", product.getSupplierName());
                productData.put("discountPercentage", product.getDiscountPercentage());
                productData.put("priceAfterDiscount", product.getPriceAfterDiscount());
                productData.put("authorNames", product.getAuthorNames());
                productData.put("publicationYear", product.getPublicationYear());
                productData.put("pageCount", product.getPageCount());
                productData.put("coverType", product.getCoverType());
                productData.put("gradeLevel", product.getGradeLevel());
                productData.put("ageRating", product.getAgeRating());
                productData.put("genres", product.getGenres());
                // Chuyển đổi danh sách ảnh thành danh sách URL
                List<String> imageUrls = new ArrayList<>(product.getImageUrls());
                productData.put("imageUrls", imageUrls);
                // Thêm vào danh sách sản phẩm
                productList.add(productData);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("products", productList);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách sản phẩm theo danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/updateDiscountPercentage/{id}")
    public ResponseEntity<Map<String, Object>> updateDiscountPercentage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Integer id,
            @RequestParam Integer discountPercentage
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để cập nhật phần trăm giảm giá");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            if (employee.getRole() == null || (employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền cập nhật phần trăm giảm giá");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            Product product = productService.findById(id);
            if (product == null) {
                response.put("status", "error");
                response.put("message", "Sản phẩm không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            if (discountPercentage < 0 || discountPercentage > 100) {
                response.put("status", "error");
                response.put("message", "Phần trăm giảm giá phải từ 0 đến 100");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            product.setDiscountPercentage(discountPercentage);
            // Cập nhật lại giá sau khi thay đổi phần trăm giảm giá
            BigDecimal discountAmount = product.getPrice()
                    .multiply(BigDecimal.valueOf(discountPercentage))
                    .divide(BigDecimal.valueOf(100));
            BigDecimal priceAfterDiscount = product.getPrice().subtract(discountAmount);
            product.setPriceAfterDiscount(priceAfterDiscount);
            productService.save(product);
            response.put("status", "success");
            response.put("message", "Cập nhật phần trăm giảm giá thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("product", product);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi cập nhật phần trăm giảm giá: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getByProductName/{productName}")
    public ResponseEntity<Map<String, Object>> getProductsByName(
            @PathVariable String productName
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> products = productService.findByProductNameLike(productName);
            if (products.isEmpty()) {
                response.put("status", "success");
                response.put("message", "Không có sản phẩm nào với tên này");
                response.put("data", Collections.emptyList());
                return ResponseEntity.ok(response);
            }
            response.put("status", "success");
            response.put("message", "Lấy danh sách sản phẩm theo tên thành công");
            List<Map<String, Object>> productList = new ArrayList<>();
            for (Product product : products) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("id", product.getId());
                productData.put("productName", product.getProductName());
                productData.put("description", product.getDescription());
                productData.put("price", product.getPrice());
//                productData.put("stockQuantity", product.getStockQuantity());
                productData.put("packageDimensions", product.getPackageDimensions());
                productData.put("weightGrams", product.getWeightGrams());
                productData.put("productType", product.getProductType());
                productData.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
                productData.put("publisherName", product.getPublisherName());
                productData.put("supplierName", product.getSupplierName());
                productData.put("discountPercentage", product.getDiscountPercentage());
                productData.put("priceAfterDiscount", product.getPriceAfterDiscount());
                productData.put("authorNames", product.getAuthorNames());
                productData.put("publicationYear", product.getPublicationYear());
                productData.put("pageCount", product.getPageCount());
                productData.put("coverType", product.getCoverType());
                productData.put("gradeLevel", product.getGradeLevel());
                productData.put("ageRating", product.getAgeRating());
                productData.put("genres", product.getGenres());
                productData.put("priceAfterDiscount", product.getPriceAfterDiscount());
                // Chuyển đổi danh sách ảnh thành danh sách URL
                List<String> imageUrls = new ArrayList<>(product.getImageUrls());
                productData.put("imageUrls", imageUrls);
                // Thêm vào danh sách sản phẩm
                productList.add(productData);
            }
            Map<String, Object> data = new HashMap<>();
            data.put("products", productList);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách sản phẩm theo tên: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
