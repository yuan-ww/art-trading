package com.example.demo.repository;

import com.example.demo.model.Artwork;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworksRepository extends JpaRepository<Artwork, Integer> {
    boolean existsByName(String name);
    @Transactional
    void deleteAllByArtworkIdIn(List<Integer> artworkIds);

    @Query("SELECT a FROM Artwork a LEFT JOIN FETCH a.images")
    List<Artwork> findAllArtworksWithImages(); // 查询所有艺术品并加载图片

    Optional<Artwork> findByArtworkId(Integer artworkId);
    List<Artwork> findByUserId(Integer userid);
}
