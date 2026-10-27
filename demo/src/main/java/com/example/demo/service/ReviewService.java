package com.example.demo.service;

import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(Integer id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getReviewsByUserId(Integer userId) {
        return reviewRepository.findByUser_UserId(userId);
    }

    public List<Review> getReviewsByOrderId(Integer orderId) {
        return reviewRepository.findByOrder_OrderId(orderId);
    }

    @Transactional
    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Integer id) {
        reviewRepository.deleteById(id);
    }
}
