package com.example.demo.model;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "Categories")
public class Category {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name="description")
    private String description;

    // 构造方法
    public Category() {
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
