package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "artworkimages")
public class ArtworkImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="image_id")
    private Integer imageId;  // 图片 ID

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "artwork_id")
    private Artwork artwork;  // 与 Artwork 的关联

    private String imageUrl;  // 图片的 URL


    // Getter 和 Setter 方法
    public Integer getId() {
        return imageId;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }
    public void setId(Integer imageId) {
        this. imageId= imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
