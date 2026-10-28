package com.example.demo.controller;
import com.example.demo.model.Favorite;
import com.example.demo.model.User;
import com.example.demo.model.Artwork;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.service.FavoriteService;
import com.example.demo.service.AccessControlService;
import com.example.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtworksRepository artworksRepository;

    @GetMapping("/getFavoritesByUser/{userId}")
    public ResponseEntity<List<Favorite>> getFavoritesByUser(@PathVariable Integer userId) {
        accessControlService.requireSelfOrAdmin(userId);
        Optional<User> user = userRepository.findById(userId);
        return user.map(value -> ResponseEntity.ok(favoriteService.getFavoritesByUser(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/addFavorite")
    public ResponseEntity<String> addFavorite(@RequestParam Integer userId, @RequestParam Integer artworkId) {
        accessControlService.requireSelfOrAdmin(userId);
        Optional<User> user = userRepository.findById(userId);
        Optional<Artwork> artwork = artworksRepository.findById(artworkId);

        if (user.isPresent() && artwork.isPresent()) {
            boolean added = favoriteService.addFavorite(user.get(), artwork.get());
            return added ? ResponseEntity.ok("收藏成功") : ResponseEntity.badRequest().body("已收藏，无需重复添加");
        }
        return ResponseEntity.badRequest().body("用户或艺术品不存在");
    }

    @DeleteMapping("/removeFavorite")
    public ResponseEntity<String> removeFavorite(@RequestParam Integer userId, @RequestParam Integer artworkId) {
        accessControlService.requireSelfOrAdmin(userId);
        Optional<User> user = userRepository.findById(userId);
        Optional<Artwork> artwork = artworksRepository.findById(artworkId);

        if (user.isPresent() && artwork.isPresent()) {
            boolean removed = favoriteService.removeFavorite(user.get(), artwork.get());
            return removed ? ResponseEntity.ok("取消收藏成功") : ResponseEntity.badRequest().body("收藏不存在");
        }
        return ResponseEntity.badRequest().body("用户或艺术品不存在");
    }
    @GetMapping("/isFavorite")
    public ResponseEntity<Boolean> isFavorite(@RequestParam Integer userId, @RequestParam Integer artworkId) {
        accessControlService.requireSelfOrAdmin(userId);
        boolean isFavorite = favoriteService.isFavorite(userId, artworkId);
        return ResponseEntity.ok(isFavorite);
    }
}
