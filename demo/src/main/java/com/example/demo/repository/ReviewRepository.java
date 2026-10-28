package com.example.demo.repository;

import com.example.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByUser_UserId(Integer userId);

    List<Review> findByOrder_OrderId(Integer orderId);

    boolean existsByOrder_OrderIdAndUser_UserId(Integer orderId, Integer userId);
}
