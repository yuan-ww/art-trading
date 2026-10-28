package com.example.demo.controller;

import com.example.demo.model.Artwork;
import com.example.demo.model.Auction;
import com.example.demo.model.Bid;
import com.example.demo.model.Category;
import com.example.demo.repository.ArtworksRepository;

import com.example.demo.service.ArtworksService;
import com.example.demo.service.AccessControlService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.demo.dto.ArtworkListItem;

@RestController
@RequestMapping("/api/artworks")
public class ArtworksController {

    @Autowired
    private ArtworksService artworksService;
    @Autowired
    private ArtworksRepository artworksRepository;
    @Autowired
    private AccessControlService accessControlService;
    // 获取所有拍品
    @GetMapping("/getAllArtworks")
    public List<Artwork> getAllCategories() {
        return artworksService.findAll();
    }
    @GetMapping("/page")
    public Page<Artwork> getArtworksPage(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) Artwork.Status status,
                                         @RequestParam(required = false) String saleType) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return artworksService.findPage(status, saleType, PageRequest.of(Math.max(page, 0), safeSize, Sort.by("artworkId").descending()));
    }
    @GetMapping("/public-page")
    public Page<ArtworkListItem> getPublicArtworksPage(@RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) List<String> categories,
                                                       @RequestParam(required = false) List<String> authors,
                                                       @RequestParam(required = false) List<Artwork.Status> statuses,
                                                       @RequestParam(required = false) List<String> types) {
        String query = keyword == null || keyword.isBlank() ? null : keyword.trim();
        boolean hasCategories = categories != null && !categories.isEmpty();
        boolean hasAuthors = authors != null && !authors.isEmpty();
        boolean hasStatuses = statuses != null && !statuses.isEmpty();
        boolean hasTypes = types != null && !types.isEmpty();
        return artworksRepository.findPublicFiltered(query, hasCategories, hasCategories ? categories : List.of(""),
                hasAuthors, hasAuthors ? authors : List.of(""), hasStatuses, hasStatuses ? statuses : List.of(Artwork.Status.在售),
                hasTypes, hasTypes ? types : List.of(""), PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("artworkId").descending()));
    }
    //
    @GetMapping("/getArtworksByUserId/{userId}")
    public List<Artwork> getBidsByAuction(@PathVariable Integer userId) {
        return artworksService.getArtworksByUserId(userId);
    }
    @GetMapping("/getAllArtworksWithImages")
    public List<Artwork> getAllArtworksWithImages() {
        return artworksRepository.findAllArtworksWithImages();
    }
    // 根据 ID 获取单个拍品
    @GetMapping("/{id}/getArtworkById")
    public ResponseEntity<Artwork> getArtworkById(@PathVariable Integer id) {
        Optional<Artwork> category = artworksService.findById(id);
        return category.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/checkArtworkExists")
    public ResponseEntity<Boolean> checkArtworkExists(@RequestParam Integer artworkId) {
        boolean exists = artworksService.existsById(artworkId);
        return ResponseEntity.ok(exists);
    }
    // 创建新拍品
    @PostMapping("/createArtwork")
    public ResponseEntity<Artwork> createArtwork( @RequestParam("name") String name,
                                                  @RequestParam("description") String description,
                                                  @RequestParam("author") String author,
                                                  @RequestParam("saleType") String saleType,
                                                  @RequestParam("categoryId") Integer categoryId,
                                                  @RequestParam("fixedPrice") BigDecimal fixedPrice,
                                                  @RequestParam("userId") Integer userId,
                                                  @RequestParam("images") MultipartFile[] images) {

        accessControlService.requireSelfOrAdmin(userId);

        Artwork artwork = new Artwork();
        artwork.setName(name);
        artwork.setDescription(description);
        artwork.setAuthor(author);
        artwork.setSaleType(saleType);
        artwork.setCategoryId(categoryId);
        artwork.setFixedPrice(fixedPrice);
        artwork.setUserId(userId);
        Artwork result = artworksService.createArtwork(artwork, images);
        return ResponseEntity.ok(result);
    }
    //添加艺术品同时创建拍卖
    @PostMapping("/createWithAuction")
    public ResponseEntity<String> createArtworkWithAuction(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("author") String author,
            @RequestParam("saleType") String saleType,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam("fixedPrice") BigDecimal fixedPrice,
            @RequestParam("userId") Integer userId,
            @ModelAttribute Auction auction,
            @RequestParam("images") MultipartFile[] images)  {
        accessControlService.requireSelfOrAdmin(userId);
        Artwork artwork = new Artwork();
        artwork.setName(name);
        artwork.setDescription(description);
        artwork.setAuthor(author);
        artwork.setSaleType(saleType);
        artwork.setCategoryId(categoryId);
        artwork.setFixedPrice(fixedPrice);
        artwork.setUserId(userId);

        try {
            // 创建艺术品并同时创建拍卖
            Artwork savedArtwork = artworksService.createArtworkWithAuction(artwork, images, auction);
            // 返回成功响应
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Artwork and auction created successfully with ID: " + savedArtwork.getArtworkId());
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            // 处理异常并返回失败响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating artwork and auction: " + e.getMessage());
        }
    }
    // 更新拍品
    @PutMapping("/{id}/updateArtwork")
    public ResponseEntity<String> updateArtwork(@PathVariable Integer id, @RequestBody Artwork artworkDetails) {
        Optional<Artwork> existingArtwork = artworksService.findById(id);
        if (existingArtwork.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        accessControlService.requireSelfOrAdmin(existingArtwork.get().getUserId());
        boolean updatedArtwork = artworksService.updateArtwork(id, artworkDetails);
        if (updatedArtwork) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/updateArtworkStatus")
    public ResponseEntity<Artwork> updateStatus(
            @RequestParam Integer artworkId,
            @RequestParam String status) {
        accessControlService.requireAdmin();
        try {
            Artwork updatedArtwork = artworksService.updateArtworkStatus(artworkId, status);
            return ResponseEntity.ok(updatedArtwork);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    // 删除拍品
    @DeleteMapping("/deleteArtwork")
    public ResponseEntity<?> deleteArtwork( @RequestBody List<Integer> artworkIds) {
        try {
            for (Integer artworkId : artworkIds) {
                Artwork artwork = artworksService.findById(artworkId).orElseThrow();
                accessControlService.requireSelfOrAdmin(artwork.getUserId());
            }
            artworksService.batchDeleteArtworks(artworkIds);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("删除失败");
        }
    }
}
