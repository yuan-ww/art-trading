package com.example.demo.service;

import com.example.demo.model.Artwork;
import com.example.demo.model.Auction;
import com.example.demo.model.Order;
import com.example.demo.model.AuctionRegistration;
import com.example.demo.model.User;
import com.example.demo.model.WalletTransaction;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.List;

@Service
public class AuctionSettlementService {

    public enum SettlementResult {
        SETTLED,
        UNSOLD,
        NOT_ENDED,
        ALREADY_PROCESSED
    }

    private final AuctionRepository auctionRepository;
    private final ArtworksRepository artworksRepository;
    private final OrderService orderService;
    private final AuctionRegistrationRepository auctionRegistrationRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;

    public AuctionSettlementService(
            AuctionRepository auctionRepository,
            ArtworksRepository artworksRepository,
            OrderService orderService,
            AuctionRegistrationRepository auctionRegistrationRepository,
            UserRepository userRepository,
            WalletService walletService,
            NotificationService notificationService
    ) {
        this.auctionRepository = auctionRepository;
        this.artworksRepository = artworksRepository;
        this.orderService = orderService;
        this.auctionRegistrationRepository = auctionRegistrationRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.notificationService = notificationService;
    }

    @Transactional
    public SettlementResult settleAuction(Integer auctionId) {
        Auction auction = auctionRepository.findByAuctionIdForUpdate(auctionId)
                .orElseThrow(() -> new NoSuchElementException("Auction not found"));

        if (auction.getStatus() != Auction.AuctionStatus.进行中) {
            return SettlementResult.ALREADY_PROCESSED;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        if (auction.getEndTime() != null && auction.getEndTime().isAfter(now)) {
            return SettlementResult.NOT_ENDED;
        }

        BigDecimal highestBid = auction.getCurrentHighestBid();
        if (highestBid == null || highestBid.compareTo(BigDecimal.ZERO) <= 0 || auction.getHighestBidder() == null) {
            refundAndClearRegistrations(auctionId);
            auction.setStatus(Auction.AuctionStatus.流拍);
            auctionRepository.save(auction);
            return SettlementResult.UNSOLD;
        }

        Artwork artwork = artworksRepository.findById(auction.getArtworkId())
                .orElseThrow(() -> new IllegalStateException("Artwork not found"));

        Order order = new Order();
        order.setArtworkId(artwork.getArtworkId());
        order.setBuyerId(auction.getHighestBidder().getUserId());
        order.setSellerId(artwork.getUserId());
        order.setFinalPrice(highestBid);
        orderService.createOrder(order);
        refundAndClearRegistrations(auctionId);

        auction.setStatus(Auction.AuctionStatus.已结束);
        auctionRepository.save(auction);
        notificationService.send(order.getBuyerId(), "拍卖 " + auctionId + " 已结束，您已中标，请在订单中付款");
        notificationService.send(order.getSellerId(), "拍卖 " + auctionId + " 已结束，订单已生成");
        return SettlementResult.SETTLED;
    }

    private void refundAndClearRegistrations(Integer auctionId) {
        List<AuctionRegistration> registrations = auctionRegistrationRepository.findByAuction_AuctionId(auctionId).stream()
                .sorted(java.util.Comparator.comparing(r -> r.getUser() == null ? null : r.getUser().getUserId(),
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))).toList();
        for (AuctionRegistration registration : registrations) {
            BigDecimal deposit = registration.getDepositAmount();
            User registeredUser = registration.getUser();
            User user = registeredUser == null ? null : userRepository.findByUserIdForUpdate(registeredUser.getUserId())
                    .orElseThrow(() -> new IllegalStateException("报名用户不存在"));
            if (Boolean.TRUE.equals(registration.getDepositPaid())
                    && deposit != null && deposit.signum() > 0 && user != null) {
                BigDecimal balance = user.getWalletBalance() == null ? BigDecimal.ZERO : user.getWalletBalance();
                user.setWalletBalance(balance.add(deposit));
                userRepository.save(user);
                walletService.record(user, deposit, WalletTransaction.Type.REFUND, "退还拍卖保证金 #" + auctionId);
            }
            auctionRegistrationRepository.delete(registration);
        }
    }
}
