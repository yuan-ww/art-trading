package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.awt.image.BufferedImage;

@Service
public class ImageUploadService {
    private static final long MAX_FILE_SIZE_BYTES = 5_000_000L;
    private static final int MAX_IMAGE_DIMENSION = 8_192;

    public void validateAll(MultipartFile[] files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            validate(file);
        }
    }

    public String store(MultipartFile file, Path directory, String publicUrlPrefix) {
        ValidatedImage image = validate(file);
        String fileName = UUID.randomUUID() + image.extension();
        Path safeDirectory = directory.toAbsolutePath().normalize();
        Path destination = safeDirectory.resolve(fileName).normalize();

        if (!destination.getParent().equals(safeDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的图片保存路径");
        }

        try {
            Files.createDirectories(safeDirectory);
            Files.write(destination, image.bytes(), StandardOpenOption.CREATE_NEW);
            return publicUrlPrefix + fileName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "图片保存失败", e);
        }
    }

    /** Store the original and a bounded-size PNG for artwork lists. */
    public String storeArtwork(MultipartFile file, Path directory, String publicUrlPrefix) {
        String url = store(file, directory, publicUrlPrefix);
        Path original = directory.toAbsolutePath().normalize().resolve(url.substring(publicUrlPrefix.length()));
        String name = original.getFileName().toString();
        Path thumbnail = original.getParent().resolve("thumbnails").resolve(name.substring(0, name.lastIndexOf('.')) + ".png");
        try (ImageInputStream input = ImageIO.createImageInputStream(original.toFile())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new IOException("无法读取图片");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                int subsampling = Math.max(1, (int) Math.ceil(Math.max(width, height) / 400.0));
                var parameters = reader.getDefaultReadParam();
                parameters.setSourceSubsampling(subsampling, subsampling, 0, 0);
                BufferedImage image = reader.read(0, parameters);
                Files.createDirectories(thumbnail.getParent());
                if (!ImageIO.write(image, "png", thumbnail.toFile())) throw new IOException("无法写入缩略图");
            } finally { reader.dispose(); }
            return url;
        } catch (IOException | RuntimeException e) {
            try { Files.deleteIfExists(thumbnail); Files.deleteIfExists(original); } catch (IOException ignored) { }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "缩略图保存失败", e);
        }
    }

    public ValidatedImage validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "图片大小不能超过 5MB");
        }

        String contentType = file.getContentType();
        String extension = extensionFor(contentType);
        if (extension == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅支持 JPEG、PNG 或 GIF 图片");
        }

        try {
            byte[] bytes = file.getBytes();
            if (!hasExpectedSignature(bytes, contentType)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片内容与文件类型不匹配");
            }
            validateImageMetadata(bytes, contentType);
            return new ValidatedImage(bytes, extension);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无法读取图片文件", e);
        }
    }

    private void validateImageMetadata(byte[] bytes, String contentType) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件不是有效图片");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                if (!formatMatches(reader.getFormatName(), contentType)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片内容与文件类型不匹配");
                }
                if (reader.getWidth(0) > MAX_IMAGE_DIMENSION || reader.getHeight(0) > MAX_IMAGE_DIMENSION) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片尺寸不能超过 8192 像素");
                }
            } finally {
                reader.dispose();
            }
        }
    }

    private String extensionFor(String contentType) {
        if (contentType == null) {
            return null;
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            default -> null;
        };
    }

    private boolean formatMatches(String format, String contentType) {
        String normalizedFormat = format.toLowerCase(Locale.ROOT);
        return ("image/jpeg".equalsIgnoreCase(contentType) && ("jpeg".equals(normalizedFormat) || "jpg".equals(normalizedFormat)))
                || ("image/png".equalsIgnoreCase(contentType) && "png".equals(normalizedFormat))
                || ("image/gif".equalsIgnoreCase(contentType) && "gif".equals(normalizedFormat));
    }

    private boolean hasExpectedSignature(byte[] bytes, String contentType) {
        if (contentType == null) {
            return false;
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
            case "image/png" -> bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                    && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A;
            case "image/gif" -> bytes.length >= 6
                    && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F'
                    && bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a';
            default -> false;
        };
    }

    public record ValidatedImage(byte[] bytes, String extension) {
    }
}
