package com.example.demo.service;

import com.example.demo.model.Category;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    private List<Category> cachedCategories;
    private long cacheDeadline;
    private static final long CACHE_MILLIS = 30_000L;

    public synchronized List<Category> findAll() {
        if (cachedCategories == null || System.currentTimeMillis() >= cacheDeadline) {
            cachedCategories = copyCategories(categoryRepository.findAll());
            cacheDeadline = System.currentTimeMillis() + CACHE_MILLIS;
        }
        return copyCategories(cachedCategories);
    }

    private List<Category> copyCategories(List<Category> categories) {
        return categories.stream().map(c -> {
            Category copy = new Category(c.getName(), c.getDescription());
            copy.setCategoryId(c.getCategoryId());
            return copy;
        }).toList();
    }

    public Optional<Category> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    public synchronized Category save(Category category) {
        Category saved = categoryRepository.save(category);
        cachedCategories = null;
        return saved;
    }
  //创建分类
  public synchronized int createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())){
          return -1;
      }else{
          categoryRepository.save(category);
          cachedCategories = null;
          return 0;
      }
  }
    public synchronized Category update(Integer id, Category categoryDetails) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        Category saved = categoryRepository.save(category);
        cachedCategories = null;
        return saved;
    }
   //删除分类
    public synchronized void batchDeleteCategories(List<Integer> categoryIds) {
       System.out.println("Deleting categories with IDs: " + categoryIds);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categoryRepository.deleteAllByCategoryIdIn(categoryIds);
            cachedCategories = null;
        } else {
            throw new IllegalArgumentException("列表为空");
        }
    }
}
