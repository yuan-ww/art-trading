package com.example.demo.service;

import com.example.demo.model.Favorite;
import com.example.demo.model.User;
import com.example.demo.model.Artwork;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtworksRepository artworksRepository;

    public List<Favorite> getFavoritesByUser(User user) {
        return favoriteRepository.findByUser(user);
    }

    public List<Favorite> getFavoritesByArtwork(Artwork artwork) {
        return favoriteRepository.findByArtwork(artwork);
    }

    public boolean addFavorite(User user, Artwork artwork) {
        if (favoriteRepository.existsByUserAndArtwork(user, artwork)) {
            return false;  // 已经收藏，避免重复收藏
        }
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setArtwork(artwork);
        favoriteRepository.save(favorite);
        return true;
    }
    @Transactional
    public boolean removeFavorite(User user, Artwork artwork) {
        if (!favoriteRepository.existsByUserAndArtwork(user, artwork)) {
            return false;  // 收藏不存在
        }
        favoriteRepository.deleteByUserAndArtwork(user, artwork);
        return true;
    }
    public boolean isFavorite(Integer userId, Integer artworkId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Artwork> artwork = artworksRepository.findById(artworkId);
        if (user.isPresent() && artwork.isPresent()) {
            return favoriteRepository.findByUserAndArtwork(user.get(), artwork.get()).isPresent();
        }
        return false;
    }
}
