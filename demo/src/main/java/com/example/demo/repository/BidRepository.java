package com.example.demo.repository;

import com.example.demo.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Integer> {
    List<Bid> findByAuction_auctionId(Integer auctionId);
    List<Bid> findByUser_userId(Integer userId);
    boolean existsByAuction_AuctionIdAndUser_UserId(Integer auctionId, Integer userId);
    long countByAuction_AuctionId(Integer auctionId);
    List<Bid> findByBidId(Integer bidId);
    Bid getByBidId(Integer bidId);
}
