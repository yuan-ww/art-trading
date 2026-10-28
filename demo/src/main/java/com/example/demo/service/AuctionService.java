package com.example.demo.service;

import com.example.demo.model.Artwork;
import com.example.demo.model.Auction;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.repository.BidRepository;
import com.example.demo.service.AuctionSettlementService.SettlementResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private ArtworksRepository artworkRepository;
    @Autowired
    private AuctionSettlementService auctionSettlementService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AuctionRegistrationRepository auctionRegistrationRepository;
    @Autowired
    private BidRepository bidRepository;
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
        if (auction.getArtworkId() == null && auction.getArtwork() != null) {
            auction.setArtworkId(auction.getArtwork().getArtworkId());
        }
        validateAuctionDetails(auction);
        Artwork artwork = artworkRepository.findById(auction.getArtworkId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "艺术品不存在"));

        // 卖家提交的拍卖必须先审核，不能由请求体直接变成进行中或结束状态。
        auction.setStatus(Auction.AuctionStatus.审核中);
        artwork.setStatus(Artwork.Status.审核);
        artworkRepository.save(artwork);
        return auctionRepository.save(auction);
    }

    // 更新拍卖
    @Transactional
    public Auction updateAuction(Integer auctionId, Auction updatedAuction) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "拍卖不存在"));
        if (auction.getVersion() != null && !auction.getVersion().equals(updatedAuction.getVersion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "拍卖已更新，请刷新后重试");
        }
        if (auction.getStatus() != Auction.AuctionStatus.审核中) {
            throw new IllegalStateException("拍卖审核完成后不能再修改；结束状态只能由结算流程产生");
        }
        if (updatedAuction.getArtworkId() != null && !updatedAuction.getArtworkId().equals(auction.getArtworkId())) {
            throw new IllegalArgumentException("不能修改拍卖关联的艺术品");
        }
        validateAuctionDetails(updatedAuction);
        Auction.AuctionStatus requestedStatus = updatedAuction.getStatus() == null
                ? Auction.AuctionStatus.审核中
                : updatedAuction.getStatus();
        if (requestedStatus != Auction.AuctionStatus.审核中 && requestedStatus != Auction.AuctionStatus.进行中) {
            throw new IllegalArgumentException("拍卖只能保持审核中或通过审核进入进行中；结束和取消必须走专用流程");
        }

        auction.setStartingPrice(updatedAuction.getStartingPrice());
        auction.setDepositPercentage(updatedAuction.getDepositPercentage());
        auction.setDepositAmount(updatedAuction.getDepositAmount());
        auction.setEndTime(updatedAuction.getEndTime());
        auction.setStartTime(updatedAuction.getStartTime());
        auction.setStatus(requestedStatus);
        if (requestedStatus == Auction.AuctionStatus.进行中) {
            Artwork artwork = artworkRepository.findById(auction.getArtworkId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "艺术品不存在"));
            artwork.setStatus(Artwork.Status.在售);
            artworkRepository.save(artwork);
            notificationService.send(artwork.getUserId(), "拍卖 " + auctionId + " 已通过审核");
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

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("拍卖不存在"));
        if (auction.getStatus() != Auction.AuctionStatus.审核中) {
            throw new IllegalStateException("只有审核中的拍卖可以通过");
        }
        Artwork artwork = artworkRepository.findById(auction.getArtworkId())
                .orElseThrow(() -> new IllegalStateException("艺术品不存在"));
        auction.setStatus(Auction.AuctionStatus.进行中);
        artwork.setStatus(Artwork.Status.在售);
        artworkRepository.save(artwork);
        notificationService.send(artwork.getUserId(), "拍卖 " + auctionId + " 已通过审核");
        return auctionRepository.save(auction);
    }

    // 拒绝拍卖，更新状态
    @Transactional
    public Auction rejectAuction(Integer auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("拍卖不存在"));
        if (auction.getStatus() != Auction.AuctionStatus.审核中) {
            throw new IllegalStateException("只有审核中的拍卖可以拒绝");
        }
        auction.setStatus(Auction.AuctionStatus.已取消);
        Artwork artwork = artworkRepository.findById(auction.getArtworkId()).orElseThrow(() -> new IllegalStateException("艺术品不存在"));
        notificationService.send(artwork.getUserId(), "拍卖 " + auctionId + " 未通过审核，可修改后重新提交");
        return auctionRepository.save(auction);
    }

    public List<Auction> getAuctionsByArtworkIds(List<Integer> artworkIds) {
        return auctionRepository.findByArtworkIdIn(artworkIds);
    }

    public SettlementResult settleAuction(Integer auctionId) {
        return auctionSettlementService.settleAuction(auctionId);
    }
    public Page<Auction> getAuctionPage(Auction.AuctionStatus status, Pageable page) {
        return status == null ? auctionRepository.findAll(page) : auctionRepository.findByStatus(status, page);
    }

    @Transactional
    public Auction resubmitAuction(Integer auctionId, Auction details) {
        Auction auction = auctionRepository.findByAuctionIdForUpdate(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("拍卖不存在"));
        if (auction.getStatus() != Auction.AuctionStatus.已取消) throw new IllegalStateException("仅已驳回拍卖可重新提交");
        if (auction.getHighestBidder() != null || (auction.getCurrentHighestBid() != null && auction.getCurrentHighestBid().signum() > 0)) {
            throw new IllegalStateException("有历史出价的拍卖不能重新提交");
        }
        if (auctionRegistrationRepository.countByAuction_AuctionId(auctionId) > 0
                || bidRepository.countByAuction_AuctionId(auctionId) > 0) {
            throw new IllegalStateException("已有报名或出价记录的拍卖不能重新提交");
        }
        if (details.getArtworkId() != null && !details.getArtworkId().equals(auction.getArtworkId())) throw new IllegalArgumentException("不能更换艺术品");
        details.setArtworkId(auction.getArtworkId());
        validateAuctionDetails(details);
        if (!details.getEndTime().isAfter(LocalDateTime.now(ZoneId.of("Asia/Shanghai")))) {
            throw new IllegalArgumentException("结束时间必须晚于当前时间");
        }
        auction.setStartingPrice(details.getStartingPrice()); auction.setDepositPercentage(details.getDepositPercentage());
        auction.setDepositAmount(details.getDepositAmount()); auction.setStartTime(details.getStartTime()); auction.setEndTime(details.getEndTime());
        auction.setStatus(Auction.AuctionStatus.审核中);
        Artwork artwork = artworkRepository.findById(auction.getArtworkId()).orElseThrow(() -> new IllegalStateException("艺术品不存在"));
        artwork.setStatus(Artwork.Status.审核); artworkRepository.save(artwork);
        return auctionRepository.save(auction);
    }

    public void updateAuctionStatusToEnded() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        List<Auction> auctions = auctionRepository.findByEndTimeLessThanEqualAndStatus(
                now,
                Auction.AuctionStatus.进行中
        );
        for (Auction auction : auctions) {
            try {
                SettlementResult result = auctionSettlementService.settleAuction(auction.getAuctionId());
                logger.info("Processed expired auction ID: {} with result: {}", auction.getAuctionId(), result);
            } catch (RuntimeException e) {
                logger.error("Failed to process expired auction ID: {}", auction.getAuctionId(), e);
            }
        }
    }

    private void validateAuctionDetails(Auction auction) {
        if (auction.getArtworkId() == null) {
            throw new IllegalArgumentException("艺术品不能为空");
        }
        if (auction.getStartingPrice() == null || auction.getStartingPrice().signum() <= 0) {
            throw new IllegalArgumentException("起拍价必须大于 0");
        }
        if (auction.getDepositPercentage() != null && auction.getDepositPercentage().signum() < 0) {
            throw new IllegalArgumentException("保证金比例不能为负数");
        }
        if (auction.getDepositAmount() != null && auction.getDepositAmount().signum() < 0) {
            throw new IllegalArgumentException("保证金金额不能为负数");
        }
        if (auction.getStartTime() == null || auction.getEndTime() == null
                || !auction.getEndTime().isAfter(auction.getStartTime())) {
            throw new IllegalArgumentException("结束时间必须晚于开始时间");
        }
    }
}
