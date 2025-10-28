package vn.edu.iuh.fit.bookshop_be.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookshop_be.dtos.CategoryRequest;
import vn.edu.iuh.fit.bookshop_be.models.Category;
import vn.edu.iuh.fit.bookshop_be.models.Employee;
import vn.edu.iuh.fit.bookshop_be.models.Role;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.services.CategoryService;
import vn.edu.iuh.fit.bookshop_be.services.CustomerService;
import vn.edu.iuh.fit.bookshop_be.services.EmployeeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;
    private final EmployeeService employeeService;
    private final CustomerService customerService;

    public CategoryController(CategoryService categoryService, EmployeeService employeeService, CustomerService customerService) {
        this.categoryService = categoryService;
        this.employeeService = employeeService;
        this.customerService = customerService;
    }

    /**
     * Tạo mới danh mục
     *
     * @param authHeader
     * @param request
     * @return ResponseEntity với thông tin về danh mục mới được tạo
     */
    @PostMapping("/createCategory")
    public ResponseEntity<Map<String, Object>> createCategory(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CategoryRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để tạo danh mục");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            if (employee.getRole() == null || (employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền tạo danh mục");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Tên danh mục không được để trống hoặc chỉ chứa khoảng trắng");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Category category = new Category();
            category.setCategoryName(request.getCategoryName().trim());
            category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

            if (request.getParentId() != null) {
                Category parentCategory = categoryService.findById(request.getParentId());
                if (parentCategory == null || parentCategory.getParentCategory() != null) {
                    response.put("status", "error");
                    response.put("message", "Danh mục cha không tồn tại hoặc không phải là danh mục cấp 1");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                category.setParentCategory(parentCategory);
            } else {
                category.setParentCategory(null);
            }

            categoryService.save(category);

            response.put("status", "success");
            response.put("message", "Tạo danh mục thành công");
            Map<String, Object> data = new HashMap<>();
            Category categoryRender = new Category();
            categoryRender.setId(category.getId());
            categoryRender.setCategoryName(category.getCategoryName());
            categoryRender.setDescription(category.getDescription());
            data.put("category", categoryRender);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi tạo danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy tất cả danh mục
     *
     * @return ResponseEntity với danh sách tất cả danh mục
     */
    @GetMapping("/getAllCategories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy danh sách danh mục
            List<Category> categories = categoryService.getAllCategories();
            if (categories == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy danh sách danh mục");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Tạo danh sách phân cấp
            List<Category> rootCategories = categories.stream()
                    .filter(category -> category.getParentCategory() == null)
                    .map(category -> {
                        Category categoryRender = new Category();
                        categoryRender.setId(category.getId());
                        categoryRender.setCategoryName(category.getCategoryName());
                        categoryRender.setDescription(category.getDescription());
                        categoryRender.setSubCategories(categoryService.buildSubCategories(category.getSubCategories()));
                        return categoryRender;
                    }).toList();

            // Tạo response
            response.put("status", "success");
            response.put("message", "Lấy danh sách danh mục thành công");
            Map<String, Object> data = new HashMap<>();
            data.put("categories", rootCategories);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách danh mục");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy tất cả danh mục cha (danh mục cấp 1)
     *
     * @return ResponseEntity với danh sách tất cả danh mục cha
     */
    @GetMapping("/getParentCategories")
    public ResponseEntity<Map<String, Object>> getParentCategories() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy danh sách danh mục cha
            List<Category> categories = categoryService.getRootCategories();
            if (categories == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy danh sách danh mục cha");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Tạo response
            response.put("status", "success");
            response.put("message", "Lấy danh sách danh mục cha thành công");
            Map<String, Object> data = new HashMap<>();
            List<Category> categoryRenders = categories.stream()
                    .map(category -> {
                        Category categoryRender = new Category();
                        categoryRender.setId(category.getId());
                        categoryRender.setCategoryName(category.getCategoryName());
                        categoryRender.setDescription(category.getDescription());
                        return categoryRender;
                    }).toList();
            data.put("categories", categoryRenders);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách danh mục cha");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    /**
     * Cập nhật danh mục theo ID
     * @param authHeader
     * @param id
     * @return ResponseEntity với thông tin về danh mục đã cập nhật
     */
    @PutMapping("/updateCategory/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Integer id,
            @RequestBody CategoryRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Employee employee = employeeService.getEmployeeByToken(authHeader);

            if (employee == null ) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để cập nhật danh mục");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            if (employee.getRole() == null || ( employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền cập nhật danh mục");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            Category category = categoryService.getCategoryById(id);
            if (category == null) {
                response.put("status", "error");
                response.put("message", "Danh mục không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (request.getCategoryName() == null || request.getCategoryName().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Tên danh mục không được để trống");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            if (request.getParentId() != null) {
                Category parentCategory = categoryService.findById(request.getParentId());
                if (parentCategory == null) {
                    response.put("status", "error");
                    response.put("message", "Danh mục cha không tồn tại");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                if (parentCategory.getId().equals(id)) {
                    response.put("status", "error");
                    response.put("message", "Danh mục cha không thể là chính nó");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                category.setParentCategory(parentCategory);
            } else {
                category.setParentCategory(null);
            }

            category.setCategoryName(request.getCategoryName());
            category.setDescription(request.getDescription());
            categoryService.updateCategory(id,category);

            response.put("status", "success");
            response.put("message", "Cập nhật danh mục thành công");
            Map<String, Object> data = new HashMap<>();
            Category categoryRender = new Category();
            categoryRender.setId(category.getId());
            categoryRender.setCategoryName(category.getCategoryName());
            categoryRender.setDescription(category.getDescription());
            data.put("category", categoryRender);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi cập nhật danh mục: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Xóa danh mục theo ID
     * @param authHeader
     * @param id
     * @return ResponseEntity với thông tin về việc xóa danh mục
     */
    @DeleteMapping("/deleteCategory/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra token và lấy thông tin người dùng
            Employee employee = employeeService.getEmployeeByToken(authHeader);
            if (employee == null) {
                response.put("status", "error");
                response.put("message", "Bạn cần đăng nhập để xóa danh mục");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Kiểm tra quyền
            if (employee.getRole() == null || (employee.getRole() != Role.STAFF && employee.getRole() != Role.MANAGER)) {
                response.put("status", "error");
                response.put("message", "Bạn không có quyền xóa danh mục");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            // Kiểm tra tồn tại danh mục
            Category category = categoryService.getCategoryById(id);
            if (category == null) {
                response.put("status", "error");
                response.put("message", "Danh mục không tồn tại");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Kiểm tra danh mục con
            if (!category.getSubCategories().isEmpty()) {
                response.put("status", "error");
                response.put("message", "Không thể xóa danh mục có danh mục con. Vui lòng xóa các danh mục con trước");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Thực hiện xóa
            categoryService.deleteCategoryWithSubCategories(id);

            // Xác nhận xóa (kiểm tra lại sau khi xóa)
            if (categoryService.getCategoryById(id) != null) {
                response.put("status", "error");
                response.put("message", "Xóa danh mục thất bại");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("status", "success");
            response.put("message", "Xóa danh mục thành công");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi xóa danh mục");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getCategoryByPatentSlug/{parentSlug}")
    public ResponseEntity<Map<String, Object>> getCategoryByPatentSlug(@PathVariable("parentSlug") String parentSlug) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy danh sách danh mục cha
            List<Category> categories = categoryService.getCategoriesByParentSlug(parentSlug);
            if (categories == null || categories.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy danh mục với slug: " + parentSlug);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Tạo response
            response.put("status", "success");
            response.put("message", "Lấy danh sách danh mục thành công");
            Map<String, Object> data = new HashMap<>();
            List<Category> categoryRenders = categories.stream()
                    .map(category -> {
                        Category categoryRender = new Category();
                        categoryRender.setId(category.getId());
                        categoryRender.setCategoryName(category.getCategoryName());
                        categoryRender.setDescription(category.getDescription());
                        return categoryRender;
                    }).toList();
            data.put("categories", categoryRenders);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách danh mục");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getCategoryBySlug/{slug}")
    public ResponseEntity<Map<String, Object>> getCategoryBySlug(@PathVariable("slug") String slug) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy danh sách danh mục cha
            Category category = categoryService.getCategoryBySlug(slug);
            if (category == null) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy danh mục với slug: " + slug);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Tạo response
            response.put("status", "success");
            response.put("message", "Lấy danh mục thành công");
            Map<String, Object> data = new HashMap<>();
            Category categoryRender = new Category();
            categoryRender.setId(category.getId());
            categoryRender.setCategoryName(category.getCategoryName());
            categoryRender.setDescription(category.getDescription());
            data.put("category", categoryRender);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh mục");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getSubCategories")
    public ResponseEntity<Map<String, Object>> getSubCategories() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Lấy danh sách danh mục cha
            List<Category> categories = categoryService.getSubCategories();
            if (categories == null || categories.isEmpty()) {
                response.put("status", "error");
                response.put("message", "Không tìm thấy danh mục con");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Tạo response
            response.put("status", "success");
            response.put("message", "Lấy danh sách danh mục con thành công");
            Map<String, Object> data = new HashMap<>();
            List<Category> categoryRenders = categories.stream()
                    .map(category -> {
                        Category categoryRender = new Category();
                        categoryRender.setId(category.getId());
                        categoryRender.setCategoryName(category.getCategoryName());
                        categoryRender.setDescription(category.getDescription());
                        categoryRender.setParentCategory(category.getParentCategory() != null ? new Category() {{
                            setId(category.getParentCategory().getId());
                        }} : null);
                        return categoryRender;
                    }).toList();
            data.put("categories", categoryRenders);
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Lỗi khi lấy danh sách danh mục con");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}
