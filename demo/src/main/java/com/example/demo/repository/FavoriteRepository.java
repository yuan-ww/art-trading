package com.example.demo.repository;

import com.example.demo.model.Favorite;
import com.example.demo.model.User;
import com.example.demo.model.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    List<Favorite> findByUser(User user);

    List<Favorite> findByArtwork(Artwork artwork);

    boolean existsByUserAndArtwork(User user, Artwork artwork);

    void deleteByUserAndArtwork(User user, Artwork artwork);

    Optional<Favorite> findByUserAndArtwork(User user, Artwork artwork);
}
