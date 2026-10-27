package com.example.demo.repository;

import com.example.demo.model.ArtworkImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkImageRepository extends JpaRepository<ArtworkImage, Integer> {

    List<ArtworkImage> findByArtwork_artworkId(Integer artworkId);
}
