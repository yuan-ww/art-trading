package com.example.demo.service;

import com.example.demo.entity.Carousel;
import com.example.demo.model.Artwork;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.CarouselRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarouselService {

    private final CarouselRepository carouselRepository;
    private final ArtworksRepository artworksRepository;

    public CarouselService(CarouselRepository carouselRepository, ArtworksRepository artworksRepository) {
        this.carouselRepository = carouselRepository;
        this.artworksRepository = artworksRepository;
    }
    public boolean existsByArtworkId(Integer artworkId) {
        return carouselRepository.existsByArtwork_ArtworkId(artworkId);
    }

    // 添加轮播图
    public Carousel addCarousel(Integer artworkId) {
        Artwork artwork = artworksRepository.findByArtworkId(artworkId)
                .orElseThrow(() -> new RuntimeException("Artwork not found"));
        Carousel carousel = new Carousel(artwork);
        return carouselRepository.save(carousel);
    }

    // 获取所有轮播图
    public List<Carousel> getAllCarousels() {
        return carouselRepository.findAll();
    }

    // 根据ID获取轮播图
    public Optional<Carousel> getCarouselById(Integer id) {
        return carouselRepository.findById(id);
    }

    // 删除轮播图
    public void deleteCarousel(Integer id) {
        carouselRepository.deleteById(id);
    }
    @Transactional
    public void deleteCarouselByArtworkId(Integer artworkId) {
        carouselRepository.deleteByArtwork_ArtworkId(artworkId);
    }
}
