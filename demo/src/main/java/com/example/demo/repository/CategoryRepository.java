package com.example.demo.repository;

import com.example.demo.model.Category;
import com.example.demo.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Transactional
    void deleteAllByCategoryIdIn(List<Integer> categoryIds);

    boolean existsByNameAndCategoryIdNot(String name, Integer categoryId);
    Optional<Category> findByCategoryId(Integer id);
    boolean existsByName(String name);
}
