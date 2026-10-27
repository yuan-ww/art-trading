package com.example.demo.service;

import com.example.demo.model.Auction;
import com.example.demo.model.AuctionRegistration;
import com.example.demo.model.User;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    /**
     * 注册到指定拍卖
     */
    @Transactional
    public AuctionRegistration registerToAuction(Integer auctionId, Integer userId, BigDecimal depositAmount) {
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

        // 确保用户的保证金足够
        if (user.getWalletBalance().compareTo(depositAmount) < 0) {
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

        return auctionRegistrationRepository.save(registration);
    }


    public List<AuctionRegistration> getRegistrationsForAuction(Integer auctionId) {
        return auctionRegistrationRepository.findByAuction_AuctionId(auctionId);
    }

    public List<AuctionRegistration> getRegistrationsForUser(Integer userId) {
        return auctionRegistrationRepository.findByUser_UserId(userId);
    }

    public boolean getRegistrationForAuctionAndUser(Integer auctionId, Integer userId) {
        return auctionRegistrationRepository.existsByUser_UserIdAndAuction_AuctionId(userId,auctionId);
    }

    @Transactional
    public void cancelRegistration(Integer registrationId) {
        Optional<AuctionRegistration> registrationOptional = auctionRegistrationRepository.findById(registrationId);

        if (registrationOptional.isEmpty()) {
            throw new IllegalArgumentException("注册记录不存在");
        }

        AuctionRegistration registration = registrationOptional.get();
        User user = registration.getUser();

        user.setWalletBalance(user.getWalletBalance().add(registration.getDepositAmount()));
        userRepository.save(user);

        auctionRegistrationRepository.delete(registration);
    }
    @Transactional
    public void settleAuction(Integer auctionId) {
        Optional<Auction> auctionOptional = auctionRepository.findById(auctionId);
        if (auctionOptional.isEmpty()) {
            throw new IllegalArgumentException("拍卖不存在");
        }
        Auction auction = auctionOptional.get();
        Integer highestBidderId = auction.getHighestBidder().getUserId();

        if (highestBidderId == null) {
            throw new IllegalArgumentException("拍卖没有赢家");
        }
        List<AuctionRegistration> registrations = auctionRegistrationRepository.findByAuction_AuctionId(auctionId);
        for (AuctionRegistration registration : registrations) {
            User user = registration.getUser();
            if (registration.getUser().getUserId().equals(highestBidderId)) {
                continue; // 跳过退还保证金的操作
            } else {
                refundDeposit(user, registration.getDepositAmount());
            }
        }
    }

    /**
     * 退还保证金
     */
    private void refundDeposit(User user, BigDecimal depositAmount) {
        // 直接更新用户的余额
        user.setWalletBalance(user.getWalletBalance().add(depositAmount));
        userRepository.save(user);
        System.out.println("退款成功，用户ID：" + user.getUserId() + ", 退款金额：" + depositAmount);
    }
}
