package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.ArtworkImageRepository;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ArtworksService {
    @Autowired
    private AuctionService auctionService;
    private final ArtworksRepository artworksRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ArtworkImageRepository artworkImageRepository;
    private final ImageUploadService imageUploadService;
    private static final Logger logger = Logger.getLogger(ArtworksService.class.getName());
    @Value("${uploads.path}")
    private String uploadDir;
    @Value("${app.public-base-url}")
    private String publicBaseUrl;


    @Autowired
    public ArtworksService(ArtworksRepository artworksRepository,
                           CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           ArtworkImageRepository artworkImageRepository,
                           ImageUploadService imageUploadService) {
        this.artworksRepository = artworksRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.artworkImageRepository = artworkImageRepository;
        this.imageUploadService = imageUploadService;
    }
    public List<Artwork> findAll() {
        List<Artwork> artworks=artworksRepository.findAll();
        return artworks;
    }

    public Optional<Artwork> findById(Integer id) {
        return artworksRepository.findById(id);
    }
    public boolean existsById(Integer artworkId) {
        return artworksRepository.existsById(artworkId);
    }
    public Artwork save(Artwork artwork) {
        return artworksRepository.save(artwork);
    }
    //创建艺术品
    @Transactional
    public Artwork createArtwork(Artwork artwork, MultipartFile[] images) {
        imageUploadService.validateAll(images);
        Category category = categoryRepository.findByCategoryId(artwork.getCategoryId()).orElse(null);
        User user = userRepository.findByUserId(artwork.getUserId()).orElse(null);
        if (category == null || user == null) {
            throw new IllegalArgumentException("Category or User not found.");
        }
        artwork.setCategory(category);
        artwork.setUser(user);
        artwork = artworksRepository.save(artwork);

        List<String> storedImageUrls = new ArrayList<>();
        try {
        if (images != null && images.length > 0) {
            List<ArtworkImage> artworkImages = new ArrayList<>();
            for (MultipartFile image : images) {
                String imageUrl = uploadImage(image);  // 上传图片并获取 URL
                if (imageUrl == null) {
                    throw new RuntimeException("Image upload failed");
                }
                storedImageUrls.add(imageUrl);
                // 创建 ArtworkImage 实体并关联图片 URL
                ArtworkImage artworkImage = new ArtworkImage();
                artworkImage.setImageUrl(imageUrl);
                artworkImage.setArtwork(artwork);  // 关联图片与艺术品
                artworkImages.add(artworkImage);  // 添加到图片列表中
            }
            artwork.setImages(artworkImages);  // 将图片列表设置到 Artwork 中
        }

        return artworksRepository.save(artwork);  // 保存包含图片的艺术品
        } catch (RuntimeException e) {
            // 数据库事务会回滚；同步清除本次已经写入磁盘的文件。
            storedImageUrls.forEach(this::deleteImageFileQuietly);
            throw e;
        }
    }
    //添加拍品时同时创建拍卖
    @Transactional
    public Artwork createArtworkWithAuction(Artwork artwork, MultipartFile[] images,Auction auction) {
        // 保存艺术品到数据库
        Artwork savedArtwork = createArtwork(artwork, images);
        // 设置拍卖的艺术品
        auction.setArtwork(savedArtwork);
        // 保存拍卖
        auctionService.createAuction(auction);
        // 返回保存后的艺术品
        return savedArtwork;
    }
    public String uploadImage(MultipartFile file) {
        return imageUploadService.storeArtwork(file, Paths.get(uploadDir), publicBaseUrl + "/uploads/");
    }
    @Transactional
    public Artwork updateArtworkStatus(Integer artworkId, String status) {
        // 查找艺术品
        Artwork artwork = artworksRepository.findById(artworkId)
                .orElseThrow(() -> new RuntimeException("艺术品不存在"));

        // 更新状态
        artwork.setStatus(Artwork.Status.valueOf(status));

        // 保存更新
        return artworksRepository.save(artwork);
    }
    @Transactional
    public boolean updateArtwork(Integer artworkId, Artwork updatedArtwork) {
        Optional<Artwork> existingArtworkOptional = artworksRepository.findByArtworkId(artworkId);

        if (existingArtworkOptional.isPresent()) {
            // 获取 Artwork 对象
            Artwork artwork = existingArtworkOptional.get();
            if (updatedArtwork.getVersion() == null || !updatedArtwork.getVersion().equals(artwork.getVersion())) {
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "作品已更新，请刷新后重试");
            }
            // 更新艺术品的基本信息
            artwork.setName(updatedArtwork.getName());
            artwork.setDescription(updatedArtwork.getDescription());
            artwork.setAuthor(updatedArtwork.getAuthor());
            artwork.setFixedPrice(updatedArtwork.getFixedPrice());
            artwork.setSaleType(updatedArtwork.getSaleType());
            artwork.setStatus(updatedArtwork.getStatus());
            artwork.setCategoryId(updatedArtwork.getCategoryId());
            // 更新分类信息
            if (updatedArtwork.getCategoryId() != null) {
                // 查找并设置新的分类
                Optional<Category> categoryOptional = categoryRepository.findById(updatedArtwork.getCategoryId());
                if (categoryOptional.isPresent()) {
                    Category category = categoryOptional.get();
                    artwork.setCategory(category);  // 设置分类
                } else {
                    return false; // 分类未找到，返回 false
                }
            }
            // 保存更新后的艺术品
            artworksRepository.save(artwork);
            return true;
        }
        return false; // 未找到指定 ID 的艺术品
    }
    //删除拍品
    @Transactional
    public void batchDeleteArtworks(List<Integer> artworkIds) {
        if (artworkIds != null && !artworkIds.isEmpty()) {
            // 循环处理每个 artworkId
            for (Integer artworkId : artworkIds) {
                try {
                    // 查找与艺术品关联的图片
                    List<ArtworkImage> artworkImages = artworkImageRepository.findByArtwork_artworkId(artworkId);

                    // 删除图片文件和图片记录
                    for (ArtworkImage artworkImage : artworkImages) {
                        // 获取图片的路径
                        String imageUrl = artworkImage.getImageUrl();
                        // 删除图片文件
                        deleteImageFile(imageUrl);
                        // 删除图片记录
                        artworkImageRepository.delete(artworkImage);
                    }
                    // 删除艺术品记录
                    Optional<Artwork> artwork = artworksRepository.findById(artworkId);
                    if (artwork.isPresent()) {
                        artworksRepository.delete(artwork.get());
                    } else {
                        logger.log(Level.SEVERE, "艺术品记录未找到，无法删除! artworkId=" + artworkId);
                        throw new RuntimeException("艺术品记录未找到，无法删除! artworkId=" + artworkId);
                    }
                } catch (Exception e) {
                    // 记录详细错误信息
                    logger.log(Level.SEVERE, "删除艺术品和图片失败，artworkId: " + artworkId, e);
                    throw e;  // 继续抛出异常
                }
            }
        } else {
            logger.log(Level.SEVERE, "传入的 artworkIds 列表为空");
            throw new IllegalArgumentException("列表为空");
        }
    }
    // 删除图片文件的方法
    private void deleteImageFile(String imageUrl) {
        try {
            // 处理文件 URL，去掉 "file:" 前缀（如果存在）
            if (imageUrl.startsWith("file:")) {
                imageUrl = imageUrl.substring(5);
            }
            // 提取文件名
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            // 创建文件路径
            Path filePath = Paths.get(uploadDir, fileName);
            int extension = fileName.lastIndexOf('.');
            if (extension > 0) Files.deleteIfExists(Paths.get(uploadDir, "thumbnails", fileName.substring(0, extension) + ".png"));
            // 删除文件
            if (Files.exists(filePath)) {
                Files.delete(filePath);  // 删除文件
                logger.log(Level.INFO, "文件已删除: " + filePath.toString());
            } else {
                logger.log(Level.WARNING, "文件不存在: " + filePath.toString());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "删除图片文件失败: " + imageUrl, e);
            throw new RuntimeException("删除图片文件失败: " + imageUrl, e);
        }
    }
    public Page<Artwork> findPage(Artwork.Status status, String saleType, Pageable pageable) {
        if (status != null && saleType != null && !saleType.isBlank()) return artworksRepository.findByStatusAndSaleType(status, saleType, pageable);
        if (status != null) return artworksRepository.findByStatus(status, pageable);
        if (saleType != null && !saleType.isBlank()) return artworksRepository.findBySaleType(saleType, pageable);
        return artworksRepository.findAll(pageable);
    }

    private void deleteImageFileQuietly(String imageUrl) {
        try {
            deleteImageFile(imageUrl);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "回滚图片文件失败: " + imageUrl, e);
        }
    }

    public List<Artwork> getArtworksByUserId(Integer userId) {
        return artworksRepository.findByUserId(userId);
    }
}
