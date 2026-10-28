package com.example.demo.service;

import com.example.demo.model.ArtworkImage;
import com.example.demo.repository.ArtworkImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import jakarta.transaction.Transactional;

@Service
public class ArtworkImageService {

    @Autowired
    private ArtworkImageRepository artworkImageRepository;

    @Value("${uploads.path}")
    private String uploadDir;

    // 根据 imageId 删除图片
    @Transactional
    public boolean deleteImageById(Integer imageId) {
        Optional<ArtworkImage> artworkImage = artworkImageRepository.findById(imageId);
        if (artworkImage.isPresent()) {
            // 获取图片的 URL
            String imageUrl = artworkImage.get().getImageUrl();

            // 删除本地文件
            deleteLocalFile(imageUrl);
            // 文件已经丢失时仍应删除数据库引用，避免前端永久展示坏图片。
            artworkImageRepository.delete(artworkImage.get());
            return true;
        }
        return false;
    }

    // 删除本地文件的方法
    private void deleteLocalFile(String imageUrl) {
        try {
            String fileName = Path.of(imageUrl).getFileName().toString();
            Path directory = Path.of(uploadDir).toAbsolutePath().normalize();
            Path file = directory.resolve(fileName).normalize();
            if (!file.getParent().equals(directory)) {
                throw new IllegalArgumentException("无效的图片路径");
            }
            int extension = fileName.lastIndexOf('.');
            if (extension > 0) Files.deleteIfExists(directory.resolve("thumbnails").resolve(fileName.substring(0, extension) + ".png"));
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new IllegalStateException("删除图片文件失败", e);
        }
    }
}
