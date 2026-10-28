package com.example.demo.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageUploadServiceTest {
    private final ImageUploadService imageUploadService = new ImageUploadService();

    @Test
    void artworkUploadCreatesOriginalAndThumbnail(@TempDir Path directory) throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "art.png", "image/png", imageBytes("png"));
        String url = imageUploadService.storeArtwork(file, directory, "/uploads/");
        String filename = url.substring("/uploads/".length());
        assertTrue(Files.exists(directory.resolve(filename)));
        assertTrue(Files.exists(directory.resolve("thumbnails").resolve(filename)));
    }

    @Test
    void acceptsValidPng() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "portrait.png", "image/png", imageBytes("png"));

        ImageUploadService.ValidatedImage image = imageUploadService.validate(file);

        assertEquals(".png", image.extension());
    }

    @Test
    void rejectsTextPretendingToBeAnImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "not-an-image.png", "image/png", "not an image".getBytes());

        assertThrows(ResponseStatusException.class, () -> imageUploadService.validate(file));
    }

    @Test
    void rejectsMismatchedImageType() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", imageBytes("png"));

        assertThrows(ResponseStatusException.class, () -> imageUploadService.validate(file));
    }

    @Test
    void rejectsUnsupportedImageType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.webp", "image/webp", new byte[]{1, 2, 3});

        assertThrows(ResponseStatusException.class, () -> imageUploadService.validate(file));
    }

    private byte[] imageBytes(String format) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            assertDoesNotThrow(() -> ImageIO.write(image, format, output));
            return output.toByteArray();
        }
    }
}
