package com.example.demo.service;

import com.example.demo.model.Auction;
import com.example.demo.model.Bid;
import com.example.demo.model.User;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.BidRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BidService {

    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private  BidRepository bidRepository;
    @Autowired
    private UserRepository userRepository;
    // 创建出价（包含业务逻辑）
    @Transactional
    public Bid placeBid(Integer auctionId, Integer userId, BigDecimal bidAmount) {
        Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);
        Optional<User> userOptional = userRepository.findById(userId);
        if (auctionOptional.isEmpty()) {
            throw new IllegalArgumentException("拍卖不存在");
        }
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        Auction auction = auctionOptional.get();
        User user = userOptional.get();
        Bid bid =new Bid();
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setBidAmount(bidAmount);
        if (bid.getBidAmount().compareTo(auction.getCurrentHighestBid()) <= 0) {
            throw new IllegalArgumentException("出价必须高于当前价格");
        }
        // 更新拍卖当前价格
        auction.setCurrentHighestBid(bid.getBidAmount());
        auction.setHighestBidder(user);
        auctionRepository.save(auction);
        return bidRepository.save(bid);
    }

    // 获取所有出价
    public List<Bid> getAllBids() {
        return bidRepository.findAll();
    }

    // 根据ID获取出价
    public List<Bid> getBidById(Integer id) {
        return bidRepository.findByBidId(id);
    }

    // 删除出价
    @Transactional
    public void deleteBid(Integer id) {
        Bid bid = bidRepository.getByBidId(id);
        bidRepository.delete(bid);
    }

    // 获取拍卖的所有出价
    public List<Bid> getBidsByAuction(Integer auctionId) {
        return bidRepository.findByAuction_auctionId(auctionId);
    }

    // 获取用户的所有出价
    public List<Bid> getBidsByUser(Integer userId) {
        return bidRepository.findByUser_userId(userId);
    }
}
