package com.example.demo.repository;

import com.example.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByBuyerId(Integer buyerId);
    List<Order> findBySellerId(Integer sellerId);
    List<Order> findByArtworkId(Integer artworkId);

    Optional<Order> findByBuyerIdAndArtworkId(Integer userId, Integer artworkId);
}

