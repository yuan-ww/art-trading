package com.example.demo.service;

import com.example.demo.model.Review;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.model.Order;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getReviewsByUserId(Integer userId) {
        return reviewRepository.findByUser_UserId(userId);
    }

    public List<Review> getReviewsByOrderId(Integer orderId) {
        return reviewRepository.findByOrder_OrderId(orderId);
    }

    @Transactional
    public Review saveReview(Review review, Integer userId, Integer orderId) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("评分必须在 1 到 5 之间");
        }
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("评价内容不能为空");
        }
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        if (!userId.equals(order.getBuyerId())) {
            throw new IllegalArgumentException("只有订单买家可以评价");
        }
        if (order.getShippingStatus() != Order.ShippingStatus.Delivered) {
            throw new IllegalArgumentException("确认收货后才能评价");
        }
        if (reviewRepository.existsByOrder_OrderIdAndUser_UserId(orderId, userId)) {
            throw new IllegalArgumentException("该订单已经评价过");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        review.setOrder(order);
        review.setUser(user);
        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}
