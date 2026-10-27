package com.example.demo.service;

import com.example.demo.model.Artwork;
import com.example.demo.model.Auction;
import com.example.demo.model.Order;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ArtworksRepository artworkRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderService orderService; // 用于生成订单

    @Autowired
    private ArtworksRepository artworksRepository; // 用于获取艺术品
    // 获取所有拍卖
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    // 根据拍卖ID获取拍卖
    public Optional<Auction> getAuctionById(Integer auctionId) {
        return auctionRepository.findById(auctionId);
    }
    public Auction getAuctionsByArtworkId(Integer artworkId) {
        return auctionRepository.findByArtwork_ArtworkId(artworkId);
    }
    // 创建新拍卖
    @Transactional
    public Auction createAuction(Auction auction) {
        // 检查艺术品是否存在
        Optional<Artwork> artwork = artworkRepository.findByArtworkId(auction.getArtworkId());
        if (!artwork.isPresent()) {
            Optional<Artwork> newArtwork = artworkRepository.findByArtworkId(auction.getArtwork().getArtworkId());
            auction.setArtworkId(auction.getArtwork().getArtworkId());
            if (!newArtwork.isPresent()){
                throw new IllegalArgumentException("艺术品不存在");
            }
        }
        if (auction.getArtworkId() != null) {
            Artwork artwork1 = artworkRepository.findById(auction.getArtworkId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "艺术品不存在"));
            // 根据拍卖状态更新艺术品状态
            if (Auction.AuctionStatus.审核中.equals(auction.getStatus())) {
                artwork1.setStatus(Artwork.Status.valueOf("审核"));
            } else if (Auction.AuctionStatus.进行中.equals(auction.getStatus())) {
                artwork1.setStatus(Artwork.Status.valueOf("在售"));
            } else if (Auction.AuctionStatus.已结束.equals(auction.getStatus()) || Auction.AuctionStatus.已取消.equals(auction.getStatus())) {
                artwork1.setStatus(Artwork.Status.valueOf("已售"));
            }
            // 更新 artwork 信息
            artworkRepository.save(artwork1);
        }
        return auctionRepository.save(auction);
    }

    // 更新拍卖
    @Transactional
    public Auction updateAuction(Integer auctionId, Auction updatedAuction) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "拍卖不存在"));
        // 更新拍卖信息
        auction.setStartingPrice(updatedAuction.getStartingPrice());
        auction.setDepositPercentage(updatedAuction.getDepositPercentage());
        auction.setDepositAmount(updatedAuction.getDepositAmount());
        auction.setEndTime(updatedAuction.getEndTime());
        auction.setStartTime(updatedAuction.getStartTime());
        auction.setStatus(updatedAuction.getStatus());
        if (updatedAuction.getArtworkId() != null) {
            Artwork artwork = artworkRepository.findById(updatedAuction.getArtworkId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "艺术品不存在"));
            // 根据拍卖状态更新艺术品状态
            if (Auction.AuctionStatus.审核中.equals(updatedAuction.getStatus())) {
                artwork.setStatus(Artwork.Status.valueOf("审核"));
            } else if (Auction.AuctionStatus.进行中.equals(updatedAuction.getStatus())) {
                artwork.setStatus(Artwork.Status.valueOf("在售"));
            } else if (Auction.AuctionStatus.已结束.equals(updatedAuction.getStatus()) || Auction.AuctionStatus.已取消.equals(updatedAuction.getStatus())) {
                artwork.setStatus(Artwork.Status.valueOf("已售"));
            }
            // 更新 artwork 信息
            artworkRepository.save(artwork);
        }
        return auctionRepository.save(auction);
    }

    // 删除拍卖
    @Transactional
    public void deleteAuction(Integer auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "拍卖不存在"));
        auctionRepository.delete(auction);
    }

    // 审核拍卖，更新状态
    @Transactional
    public Auction approveAuction(Integer auctionId) {

        Optional<Auction> auction = auctionRepository.findById(auctionId);
        if (!auction.isPresent()) {
            throw new IllegalArgumentException("拍卖不存在");
        }
        auction.get().setStatus(Auction.AuctionStatus.进行中);  // 设置为“进行中”
        return auctionRepository.save(auction.get());
    }

    // 拒绝拍卖，更新状态
    @Transactional
    public Auction rejectAuction(Integer auctionId) {
        Optional<Auction> auction = auctionRepository.findById(auctionId);
        if (!auction.isPresent()) {
            throw new IllegalArgumentException("拍卖不存在");
        }
        auction.get().setStatus(Auction.AuctionStatus.已取消);  // 设置为“已取消”
        return auctionRepository.save(auction.get());
    }

    public List<Auction> getAuctionsByArtworkIds(List<Integer> artworkIds) {
        return auctionRepository.findByArtworkIdIn(artworkIds);
    }

    @Transactional
    public boolean settleAuction(Integer auctionId) throws Exception {
        // 获取拍卖信息
        Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);
        if (!auctionOptional.isPresent()) {
            throw new Exception("拍卖不存在");
        }
        Auction auction = auctionOptional.get();
        if (auction.getCurrentHighestBid().compareTo(BigDecimal.ZERO) <= 0) {
            auction.setStatus(Auction.AuctionStatus.流拍);
            auctionRepository.save(auction);
        }
        if (auction.getCurrentHighestBid() != null && auction.getHighestBidder() != null) {
            Integer artworkId = auction.getArtworkId();
            Integer buyerId = auction.getHighestBidder().getUserId();
            Integer senderId = null;
            Optional<Artwork> artworkOptional = artworksRepository.findById(artworkId);
            if (artworkOptional.isPresent()) {
                Artwork artwork = artworkOptional.get();
                senderId = artwork.getUserId();
            } else {
                throw new RuntimeException("艺术品未找到，无法获取卖方信息");
            }
            BigDecimal finalPrice = auction.getCurrentHighestBid();

            Order order = new Order();
            order.setArtworkId(artworkId);
            order.setBuyerId(buyerId);
            order.setSellerId(senderId);
            order.setFinalPrice(finalPrice);
            orderService.createOrder(order);
            Artwork artwork = artworkOptional.get();
            artwork.setStatus(Artwork.Status.已售);
            artworksRepository.save(artwork);

            auction.setStatus(Auction.AuctionStatus.已结束);
            auctionRepository.save(auction);
            return true;
        } else {
            throw new Exception("拍卖没有出价，无法结算");
        }
    }
    public void updateAuctionStatusToEnded() {
        LocalDateTime currentTime = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        System.out.println("Current time: " + currentTime);
        LocalDateTime timePlus8Hours = currentTime.plusHours(8);
        List<Auction> auctions = auctionRepository.findByEndTimeLessThanEqualAndStatus(timePlus8Hours, Auction.AuctionStatus.valueOf("进行中"));
        System.out.println("查询到的拍卖数量: " + auctions.size());
        if (auctions.isEmpty()) {
            System.out.println("没有找到符合条件的拍卖");
        } else {
            auctions.forEach(auction -> {
                System.out.println("处理拍卖ID: " + auction.getAuctionId());  // 打印拍卖ID
                try {
                    settleAuction(auction.getAuctionId());
                } catch (Exception e) {
                    System.err.println("处理拍卖时出错: " + e.getMessage());
                }
            });
        }
    }
}
