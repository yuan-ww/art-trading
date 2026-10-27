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

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> findById(Integer id) {
        return categoryRepository.findById(id);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }
  //创建分类
  public int createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())){
          return -1;
      }else{
          categoryRepository.save(category);
          return 0;
      }
  }
    public Category update(Integer id, Category categoryDetails) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        return categoryRepository.save(category);
    }
   //删除分类
    public void batchDeleteCategories(List<Integer> categoryIds) {
       System.out.println("Deleting categories with IDs: " + categoryIds);
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categoryRepository.deleteAllByCategoryIdIn(categoryIds);
        } else {
            throw new IllegalArgumentException("列表为空");
        }
    }
}
