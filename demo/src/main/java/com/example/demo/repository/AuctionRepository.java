package com.example.demo.repository;

import com.example.demo.model.Auction;
import com.example.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Integer> {

    Auction findByArtwork_ArtworkId(Integer artworkId);
    Optional<Auction> findByAuctionId(Integer auctionId);

    List<Auction> findByArtworkIdIn(List<Integer> artworkIds);

    List<Auction> findByEndTimeLessThanEqualAndStatus(LocalDateTime endTime, Auction.AuctionStatus status);
    // 可根据需要自定义查询方法
}
