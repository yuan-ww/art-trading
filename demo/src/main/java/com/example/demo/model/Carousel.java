package com.example.demo.entity;

import com.example.demo.model.Artwork;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Carousel")
public class Carousel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer carouselId; // 轮播ID

    @ManyToOne
    @JoinColumn(name = "artwork_id", nullable = false)
    private Artwork artwork; // 关联的艺术品

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now(); // 创建时间

    // 构造方法
    public Carousel() {}

    public Carousel(Artwork artwork) {
        this.artwork = artwork;
    }

    // Getter 和 Setter 方法
    public Integer getCarouselId() {
        return carouselId;
    }

    public void setCarouselId(Integer carouselId) {
        this.carouselId = carouselId;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
