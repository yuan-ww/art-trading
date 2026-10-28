package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "Artworks")
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artwork_id")
    private Integer artworkId;  // 艺术品ID

    @Column(name = "name", nullable = false)
    private String name;  // 艺术品名称

    @Column(name = "description")
    private String description;  // 艺术品描述

    @Column(name = "author", nullable = false)
    private String author;  // 艺术品作者

    @Column(name = "sale_type", nullable = false)
    private String saleType;

    // Fixed price for sale type
    @Column(name = "fixed_price", precision = 10, scale = 2)
    private BigDecimal fixedPrice;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.审核;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;  // 创建时间

    @Column(name = "updated_at")
    private Timestamp updatedAt;  // 更新时间
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "user_id")
    private Integer userId;
    // 外键关联分类表
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id" ,insertable = false, updatable = false)
    private Category category;

    // 外键关联用户表
    @ManyToOne
    @JsonIgnoreProperties({"email", "password", "phone", "realName", "idCardNumber", "address", "walletBalance", "role"})
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @OneToMany(mappedBy = "artwork", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ArtworkImage> images; // 关联的图片集合

    public enum Status {
        在售,
        已售,
        审核,
    }
    // Getters and Setters

    public Integer getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(Integer artworkId) {
        this.artworkId = artworkId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getSaleType() {
        return saleType;
    }

    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }

    public BigDecimal getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(BigDecimal fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
