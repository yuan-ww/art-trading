package com.example.demo.controller;

import com.example.demo.entity.Carousel;
import com.example.demo.service.CarouselService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carousel")
public class CarouselController {

    private final CarouselService carouselService;

    public CarouselController(CarouselService carouselService) {
        this.carouselService = carouselService;
    }

    // 添加轮播图
    @PostMapping("/add/{artworkId}")
    public ResponseEntity<?> addCarousel(@PathVariable Integer artworkId) {
        if (carouselService.existsByArtworkId(artworkId)) {
            return ResponseEntity.ok(false);
        }
        Carousel carousel = carouselService.addCarousel(artworkId);
        return ResponseEntity.ok(carousel);
    }
    // 获取所有轮播图
    @GetMapping("/getAllCarousels")
    public ResponseEntity<List<Carousel>> getAllCarousels() {
        return ResponseEntity.ok(carouselService.getAllCarousels());
    }

    // 根据ID获取轮播图
    @GetMapping("/getCarouselById/{id}")
    public ResponseEntity<Carousel> getCarouselById(@PathVariable Integer id) {
        Optional<Carousel> carousel = carouselService.getCarouselById(id);
        return carousel.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 删除轮播图
    @DeleteMapping("/deleteCarousel/{id}")
    public ResponseEntity<String> deleteCarousel(@PathVariable Integer id) {
        carouselService.deleteCarousel(id);
        return ResponseEntity.ok("Carousel deleted successfully");
    }
    @DeleteMapping("/deleteCarouselByArtworkId/{artworkId}")
    public ResponseEntity<String> deleteCarouselByArtworkId(@PathVariable Integer artworkId) {
        carouselService.deleteCarouselByArtworkId(artworkId);
        return ResponseEntity.ok("Carousel deleted successfully by artworkId: " + artworkId);
    }
}
