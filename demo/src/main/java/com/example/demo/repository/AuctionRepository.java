package com.example.demo.repository;

import com.example.demo.model.Auction;
import com.example.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuctionRepository extends JpaRepository<Auction, Integer> {

    Auction findByArtwork_ArtworkId(Integer artworkId);
    Optional<Auction> findByAuctionId(Integer auctionId);
    Page<Auction> findByStatus(Auction.AuctionStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select auction from Auction auction where auction.auctionId = :auctionId")
    Optional<Auction> findByAuctionIdForUpdate(@Param("auctionId") Integer auctionId);

    List<Auction> findByArtworkIdIn(List<Integer> artworkIds);

    List<Auction> findByEndTimeLessThanEqualAndStatus(LocalDateTime endTime, Auction.AuctionStatus status);
    // 可根据需要自定义查询方法
}
