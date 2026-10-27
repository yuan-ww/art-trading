package com.example.demo.service;

import com.example.demo.model.ArtworkImage;
import com.example.demo.repository.ArtworkImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

@Service
public class ArtworkImageService {

    @Autowired
    private ArtworkImageRepository artworkImageRepository;

    @Value("${uploads.path}")
    private String uploadDir;

    // 根据 imageId 删除图片
    public boolean deleteImageById(Integer imageId) {
        Optional<ArtworkImage> artworkImage = artworkImageRepository.findById(imageId);
        if (artworkImage.isPresent()) {
            // 获取图片的 URL
            String imageUrl = artworkImage.get().getImageUrl();

            // 删除本地文件
            boolean fileDeleted = deleteLocalFile(imageUrl);

            if (fileDeleted) {
                // 删除数据库记录
                artworkImageRepository.deleteById(imageId);
                return true;
            }
        }
        return false;
    }

    // 删除本地文件的方法
    private boolean deleteLocalFile(String imageUrl) {
        try {
            if (imageUrl.startsWith("file:")) {
                imageUrl = imageUrl.substring(5);
            }
            // 提取文件名
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            // 创建文件对象
            File file = new File(uploadDir, fileName);
            // 删除文件
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
