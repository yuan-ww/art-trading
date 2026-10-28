package com.example.demo.service;

import com.example.demo.model.Auction;
import com.example.demo.model.Bid;
import com.example.demo.model.User;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.BidRepository;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    @Autowired
    private AuctionRegistrationRepository auctionRegistrationRepository;
    @Autowired
    private NotificationService notificationService;
    // 创建出价（包含业务逻辑）
    @Transactional
    public Bid placeBid(Integer auctionId, Integer userId, BigDecimal bidAmount) {
        Optional<Auction> auctionOptional = auctionRepository.findByAuctionIdForUpdate(auctionId);
        Optional<User> userOptional = userRepository.findById(userId);
        if (auctionOptional.isEmpty()) {
            throw new IllegalArgumentException("拍卖不存在");
        }
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        Auction auction = auctionOptional.get();
        User user = userOptional.get();
        if (auction.getArtwork() != null && userId.equals(auction.getArtwork().getUserId())) {
            throw new IllegalArgumentException("不能竞拍自己的艺术品");
        }
        if (auction.getStatus() != Auction.AuctionStatus.进行中) {
            throw new IllegalArgumentException("拍卖尚未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        if ((auction.getStartTime() != null && now.isBefore(auction.getStartTime()))
                || (auction.getEndTime() != null && !now.isBefore(auction.getEndTime()))) {
            throw new IllegalArgumentException("不在出价时间内");
        }
        if (!auctionRegistrationRepository.existsByUser_UserIdAndAuction_AuctionId(userId, auctionId)) {
            throw new IllegalArgumentException("请先报名拍卖");
        }
        if (bidAmount == null || bidAmount.signum() <= 0) {
            throw new IllegalArgumentException("出价金额无效");
        }
        Bid bid =new Bid();
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setBidAmount(bidAmount);
        BigDecimal currentPrice = auction.getCurrentHighestBid() == null ? BigDecimal.ZERO : auction.getCurrentHighestBid();
        BigDecimal startingPrice = auction.getStartingPrice() == null ? BigDecimal.ZERO : auction.getStartingPrice();
        BigDecimal minimumPrice = currentPrice.max(startingPrice);
        if (bid.getBidAmount().compareTo(minimumPrice) <= 0) {
            throw new IllegalArgumentException("出价必须高于当前价格");
        }
        // 更新拍卖当前价格
        User previousBidder = auction.getHighestBidder();
        auction.setCurrentHighestBid(bid.getBidAmount());
        auction.setHighestBidder(user);
        auctionRepository.save(auction);
        if (previousBidder != null && !userId.equals(previousBidder.getUserId())) {
            notificationService.send(previousBidder.getUserId(), "拍卖 " + auctionId + " 出现更高出价");
        }
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

    public Optional<Bid> findBidById(Integer id) {
        return bidRepository.findById(id);
    }

    // 删除出价
    @Transactional
    public void deleteBid(Integer id) {
        Bid bid = bidRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("出价不存在"));
        Auction auction = auctionRepository.findByAuctionIdForUpdate(bid.getAuction().getAuctionId())
                .orElseThrow(() -> new EntityNotFoundException("拍卖不存在"));
        LocalDateTime now = LocalDateTime.now(java.time.ZoneId.of("Asia/Shanghai"));
        if (auction.getStatus() != Auction.AuctionStatus.进行中
                || (auction.getEndTime() != null && !auction.getEndTime().isAfter(now))) {
            throw new IllegalStateException("拍卖已结束，不能删除出价");
        }
        bidRepository.delete(bid);
        refreshHighestBid(auction, id);
        auctionRepository.save(auction);
    }

    private void refreshHighestBid(Auction auction, Integer deletedBidId) {
        Optional<Bid> highestRemainingBid = bidRepository.findByAuction_auctionId(auction.getAuctionId()).stream()
                .filter(candidate -> !candidate.getBidId().equals(deletedBidId))
                .max(Comparator.comparing(Bid::getBidAmount)
                        .thenComparing(Bid::getBidTime, Comparator.nullsFirst(Comparator.naturalOrder())));

        if (highestRemainingBid.isEmpty()) {
            auction.setCurrentHighestBid(BigDecimal.ZERO);
            auction.setHighestBidder(null);
            return;
        }

        Bid highestBid = highestRemainingBid.get();
        auction.setCurrentHighestBid(highestBid.getBidAmount());
        auction.setHighestBidder(highestBid.getUser());
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
