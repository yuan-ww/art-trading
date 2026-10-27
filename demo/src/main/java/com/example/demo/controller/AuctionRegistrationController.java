package com.example.demo.controller;

import com.example.demo.model.AuctionRegistration;
import com.example.demo.service.AuctionRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/auctionRegistrations")
public class AuctionRegistrationController {

    @Autowired
    private AuctionRegistrationService auctionRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<String> registerForAuction(@RequestParam Integer auctionId,
                                                     @RequestParam Integer userId,
                                                     @RequestParam BigDecimal depositAmount) {
        try {
            auctionRegistrationService.registerToAuction(auctionId, userId, depositAmount);
            return ResponseEntity.ok("注册成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<AuctionRegistration>> getRegistrationsForAuction(@PathVariable Integer auctionId) {
        List<AuctionRegistration> registrations = auctionRegistrationService.getRegistrationsForAuction(auctionId);
        return ResponseEntity.ok(registrations);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuctionRegistration>> getRegistrationsForUser(@PathVariable Integer userId) {
        List<AuctionRegistration> registrations = auctionRegistrationService.getRegistrationsForUser(userId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/isUserRegistered/{auctionId}/user/{userId}")
    public ResponseEntity<Boolean> getRegistrationForAuctionAndUser(
            @PathVariable Integer auctionId,
            @PathVariable Integer userId) {
        boolean isRegistered = auctionRegistrationService.getRegistrationForAuctionAndUser(auctionId, userId);

        return ResponseEntity.ok(isRegistered);
    }

    @DeleteMapping("/cancel/{registrationId}")
    public ResponseEntity<String> cancelRegistration(@PathVariable Integer registrationId) {
        try {
            auctionRegistrationService.cancelRegistration(registrationId);
            return ResponseEntity.ok("取消注册成功");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/settleDeposit/{auctionId}")
    public ResponseEntity<String> settleDeposit(@PathVariable Integer auctionId) {
        try {
            auctionRegistrationService.settleAuction(auctionId);
            return ResponseEntity.ok("拍卖已结算，保证金已退还，非赢家已移除");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("错误: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("服务器错误: " + e.getMessage());
        }
    }
}
