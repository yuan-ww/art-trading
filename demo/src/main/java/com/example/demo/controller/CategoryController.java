package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private CategoryRepository categoryRepository ;
    @Autowired
    private AccessControlService accessControlService;
    // 获取所有分类
    @GetMapping("/getAllCategories")
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    // 根据 ID 获取单个分类
    @GetMapping("/{id}/getCategoryById")
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        Optional<Category> category = categoryService.findById(id);
        return category.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/checkCategoryName")
    public ResponseEntity<Boolean> checkUsername(@RequestParam String name, @RequestParam Integer Id) {
        boolean exists = categoryRepository.existsByNameAndCategoryIdNot(name, Id);
        return ResponseEntity.ok(exists);
    }
    // 创建新分类
    @PostMapping("/createCategory")
    public int createCategory(@RequestBody Category category) {
        accessControlService.requireAdmin();
        return categoryService.createCategory(category);
    }

    // 更新分类
    @PutMapping("/{id}/updateCategory")
    public ResponseEntity<Category> updateCategory(@PathVariable Integer id, @RequestBody Category categoryDetails) {
        accessControlService.requireAdmin();
        Optional<Category> category = categoryService.findById(id);
        if (category.isPresent()) {
            Category updatedCategory = categoryService.update(id, categoryDetails);
            return ResponseEntity.ok(updatedCategory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 删除分类
    @DeleteMapping("/deleteCategory")
    public ResponseEntity<?> deleteCategory( @RequestBody List<Integer> categoryIds) {
        try {
            accessControlService.requireAdmin();
            categoryService.batchDeleteCategories(categoryIds);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除失败");
        }
    }
}
