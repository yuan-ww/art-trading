package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.Review;
import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ReviewService reviewService;

    @Test
    void deliveredOrderCanBeReviewedOnce() {
        Order order = deliveredOrder();
        User buyer = new User();
        buyer.setUserId(5);
        Review review = validReview();
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrder_OrderIdAndUser_UserId(1, 5)).thenReturn(false);
        when(userRepository.findById(5)).thenReturn(Optional.of(buyer));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review saved = reviewService.saveReview(review, 5, 1);

        assertEquals(order, saved.getOrder());
        assertEquals(buyer, saved.getUser());
    }

    @Test
    void duplicateReviewIsRejectedWhileOrderIsLocked() {
        Order order = deliveredOrder();
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));
        when(reviewRepository.existsByOrder_OrderIdAndUser_UserId(1, 5)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> reviewService.saveReview(validReview(), 5, 1));
    }

    private Order deliveredOrder() {
        Order order = new Order();
        order.setOrderId(1);
        order.setBuyerId(5);
        order.setShippingStatus(Order.ShippingStatus.Delivered);
        return order;
    }

    private Review validReview() {
        Review review = new Review();
        review.setRating(5);
        review.setComment("很好");
        return review;
    }
}
