package com.example.demo.controller;

import com.example.demo.model.Review;
import com.example.demo.service.ReviewService;
import com.example.demo.service.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private AccessControlService accessControlService;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/getReviewById/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        return review.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/getReviewsByUserId/{userId}")
    public List<Review> getReviewsByUserId(@PathVariable Integer userId) {
        return reviewService.getReviewsByUserId(userId);
    }

    @GetMapping("/getReviewsByOrderId/{orderId}")
    public List<Review> getReviewsByOrderId(@PathVariable Integer orderId) {
        return reviewService.getReviewsByOrderId(orderId);
    }

    @PostMapping("/createReview")
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        if (review.getUser() == null || review.getUser().getUserId() == null
                || review.getOrder() == null || review.getOrder().getOrderId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Integer userId = review.getUser().getUserId();
        Integer orderId = review.getOrder().getOrderId();
        accessControlService.requireSelfOrAdmin(userId);
        Review savedReview = reviewService.saveReview(review, userId, orderId);
        return ResponseEntity.ok(savedReview);
    }

    @DeleteMapping("/deleteReview/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        Review review = reviewService.getReviewById(id)
                .orElseThrow(() -> new IllegalArgumentException("评价不存在"));
        accessControlService.requireSelfOrAdmin(review.getUser().getUserId());
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
