package com.example.demo.service;

import com.example.demo.model.Auction;
import com.example.demo.model.AuctionRegistration;
import com.example.demo.model.User;
import com.example.demo.model.WalletTransaction;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.BidRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionRegistrationService {

    @Autowired
    private AuctionRegistrationRepository auctionRegistrationRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private WalletService walletService;

    /**
     * 注册到指定拍卖
     */
    @Transactional
    public AuctionRegistration registerToAuction(Integer auctionId, Integer userId, BigDecimal depositAmount) {
        Optional<Auction> auctionOptional = auctionRepository.findByAuctionIdForUpdate(auctionId);
        Optional<User> userOptional = userRepository.findByUserIdForUpdate(userId);

        if (auctionOptional.isEmpty()) {
            throw new IllegalArgumentException("拍卖不存在");
        }

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }

        Auction auction = auctionOptional.get();
        User user = userOptional.get();

        if (auction.getArtwork() != null && userId.equals(auction.getArtwork().getUserId())) {
            throw new IllegalArgumentException("不能报名自己的拍卖");
        }

        if (auction.getStatus() != Auction.AuctionStatus.进行中) {
            throw new IllegalArgumentException("拍卖尚未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        if ((auction.getStartTime() != null && now.isBefore(auction.getStartTime()))
                || (auction.getEndTime() != null && !now.isBefore(auction.getEndTime()))) {
            throw new IllegalArgumentException("不在报名时间内");
        }
        if (auctionRegistrationRepository.existsByUser_UserIdAndAuction_AuctionId(userId, auctionId)) {
            throw new IllegalArgumentException("已报名该拍卖");
        }
        if (auction.getDepositAmount() == null || auction.getDepositAmount().signum() < 0) {
            throw new IllegalArgumentException("拍卖保证金无效");
        }
        depositAmount = auction.getDepositAmount();

        // 确保用户的保证金足够
        if (user.getWalletBalance() == null || user.getWalletBalance().compareTo(depositAmount) < 0) {
            throw new IllegalArgumentException("用户余额不足");
        }
        // 创建注册记录
        AuctionRegistration registration = new AuctionRegistration();
        registration.setAuction(auction);
        registration.setUser(user);
        registration.setDepositPaid(true);
        registration.setDepositAmount(depositAmount);
        LocalDateTime currentTime = LocalDateTime.now();
        registration.setRegistrationTime(currentTime);
        // 扣除用户的余额
        user.setWalletBalance(user.getWalletBalance().subtract(depositAmount));
        userRepository.save(user);
        walletService.record(user, depositAmount.negate(), WalletTransaction.Type.PAYMENT, "拍卖保证金 #" + auctionId);

        return auctionRegistrationRepository.save(registration);
    }


    public List<AuctionRegistration> getRegistrationsForAuction(Integer auctionId) {
        return auctionRegistrationRepository.findByAuction_AuctionId(auctionId);
    }

    public long countRegistrationsForAuction(Integer auctionId) {
        return auctionRegistrationRepository.countByAuction_AuctionId(auctionId);
    }

    public List<AuctionRegistration> getRegistrationsForUser(Integer userId) {
        return auctionRegistrationRepository.findByUser_UserId(userId);
    }

    public boolean getRegistrationForAuctionAndUser(Integer auctionId, Integer userId) {
        return auctionRegistrationRepository.existsByUser_UserIdAndAuction_AuctionId(userId,auctionId);
    }

    public Optional<AuctionRegistration> getRegistrationById(Integer registrationId) {
        return auctionRegistrationRepository.findById(registrationId);
    }

    @Transactional
    public void cancelRegistration(Integer registrationId) {
        Optional<AuctionRegistration> registrationOptional = auctionRegistrationRepository.findById(registrationId);

        if (registrationOptional.isEmpty()) {
            throw new IllegalArgumentException("注册记录不存在");
        }

        AuctionRegistration registration = registrationOptional.get();
        Auction auction = auctionRepository.findByAuctionIdForUpdate(registration.getAuction().getAuctionId())
                .orElseThrow(() -> new IllegalArgumentException("拍卖不存在"));
        User user = userRepository.findByUserIdForUpdate(registration.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        if (auction.getStatus() != Auction.AuctionStatus.进行中
                || (auction.getEndTime() != null && !auction.getEndTime().isAfter(now))) {
            throw new IllegalStateException("拍卖已结束，不能取消报名");
        }
        if (bidRepository.existsByAuction_AuctionIdAndUser_UserId(auction.getAuctionId(), user.getUserId())) {
            throw new IllegalStateException("已有出价记录，不能取消报名");
        }

        if (Boolean.TRUE.equals(registration.getDepositPaid())
                && registration.getDepositAmount() != null
                && registration.getDepositAmount().signum() > 0) {
            BigDecimal balance = user.getWalletBalance() == null ? BigDecimal.ZERO : user.getWalletBalance();
            user.setWalletBalance(balance.add(registration.getDepositAmount()));
            userRepository.save(user);
            walletService.record(user, registration.getDepositAmount(), WalletTransaction.Type.REFUND, "退还拍卖保证金 #" + auction.getAuctionId());
        }

        auctionRegistrationRepository.delete(registration);
    }
    public void settleAuction(Integer auctionId) {
        Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);
        if (auctionOptional.isEmpty()) {
            throw new IllegalArgumentException("拍卖不存在");
        }
        Auction auction = auctionOptional.get();
        if (auction.getStatus() == Auction.AuctionStatus.进行中) {
            throw new IllegalArgumentException("请先结算拍卖，保证金会自动处理");
        }
    }
}
