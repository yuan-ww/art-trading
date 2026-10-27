package com.example.demo.repository;

import com.example.demo.entity.Carousel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarouselRepository extends JpaRepository<Carousel, Integer> {

    boolean existsByArtwork_ArtworkId(Integer artworkId);
    void deleteByArtwork_ArtworkId(Integer artworkId);
}
