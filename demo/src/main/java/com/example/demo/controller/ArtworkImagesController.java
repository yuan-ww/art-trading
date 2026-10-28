package com.example.demo.controller;

import com.example.demo.model.ArtworkImage;
import com.example.demo.repository.ArtworkImageRepository;
import com.example.demo.service.ArtworkImageService;
import com.example.demo.service.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artworkImages")
public class ArtworkImagesController {

    @Autowired
    private ArtworkImageRepository artworkImageRepository;
    @Autowired
    private ArtworkImageService artworkImageService;
    @Autowired
    private AccessControlService accessControlService;

    // 删除图片接口
    @DeleteMapping("/deleteImage/{imageId}")
    public ResponseEntity<String> deleteImage(@PathVariable Integer imageId) {
        ArtworkImage image = artworkImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("未找到该图片"));
        accessControlService.requireSelfOrAdmin(image.getArtwork().getUserId());
        try {
            boolean success = artworkImageService.deleteImageById(imageId);
            if (success) {
                return ResponseEntity.ok("图片删除成功");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未找到该图片");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("图片删除失败");
        }
    }
    // 根据艺术品 ID 获取所有图片
    @GetMapping("/getImage/{artworkId}")
    public List<ArtworkImage> getImagesByArtworkId(@PathVariable("artworkId") Integer artworkId) {
        return artworkImageRepository.findByArtwork_artworkId(artworkId);
    }
}
