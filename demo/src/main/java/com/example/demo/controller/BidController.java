package com.example.demo.controller;

import com.example.demo.model.Bid;
import com.example.demo.service.AuctionRegistrationService;
import com.example.demo.service.BidService;
import com.example.demo.service.AccessControlService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bids")
public class BidController {

    @Autowired
    private BidService bidService;
    @Autowired
    private AccessControlService accessControlService;


    // 创建出价
    @PostMapping("createBid")
    public ResponseEntity<?> createBid(@RequestParam Integer auctionId,
                                       @RequestParam Integer userId,
                                       @RequestParam BigDecimal bidAmount){
        try {
            accessControlService.requireSelfOrAdmin(userId);
            Bid savedBid = bidService.placeBid(auctionId,userId,bidAmount);
            return ResponseEntity.ok(savedBid);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 获取所有出价
    @GetMapping("getAllBid")
    public List<Bid> getAllBids() {
        return bidService.getAllBids();
    }

    // 获取单个出价
    @GetMapping("getBid/{id}")
    public ResponseEntity<List<Bid>> getBidById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(bidService.getBidById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 删除出价
    @DeleteMapping("deleteBid/{id}")
    public ResponseEntity<?> deleteBid(@PathVariable Integer id) {
        try {
            Bid bid = bidService.findBidById(id)
                    .orElseThrow(() -> new EntityNotFoundException("出价不存在"));
            accessControlService.requireSelfOrAdmin(bid.getUser().getUserId());
            bidService.deleteBid(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 获取拍卖相关出价
    @GetMapping("/getForAuction/{auctionId}")
    public List<Bid> getBidsByAuction(@PathVariable Integer auctionId) {
        return bidService.getBidsByAuction(auctionId);
    }

    // 获取用户出价记录
    @GetMapping("/getUserBid/{userId}")
    public List<Bid> getBidsByUser(@PathVariable Integer userId) {
        accessControlService.requireSelfOrAdmin(userId);
        return bidService.getBidsByUser(userId);
    }
}
