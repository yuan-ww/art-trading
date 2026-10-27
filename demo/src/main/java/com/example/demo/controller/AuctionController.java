package com.example.demo.controller;

import com.example.demo.model.Auction;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.service.AuctionRegistrationService;
import com.example.demo.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;
    @Autowired
    private AuctionRegistrationService auctionRegistrationService;

    // 获取所有拍卖
    @GetMapping("/getAuction")
    public ResponseEntity<List<Auction>> getAllAuctions() {
        List<Auction> auctions = auctionService.getAllAuctions();
        System.out.println("Fetched auctions: " + auctions);
        return ResponseEntity.ok(auctions);
    }
    // 根据artworkId获取拍卖
    @GetMapping("/getAuctionsByArtworkId/{artworkId}")
    public ResponseEntity<Auction> getAuctionsByArtworkId(@PathVariable Integer artworkId) {
        Auction auction = auctionService.getAuctionsByArtworkId(artworkId);
        return ResponseEntity.ok(auction);
    }
    // 获取单个拍卖
    @GetMapping("/getAuctionById/{auctionId}")
    public ResponseEntity<Auction> getAuctionById(@PathVariable Integer auctionId) {
        Optional<Auction> auction = auctionService.getAuctionById(auctionId);
        if (auction.isPresent()) {
            return ResponseEntity.ok(auction.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 创建新的拍卖
    @PostMapping("createAuction")
    public ResponseEntity<Auction> createAuction(@RequestBody Auction auction) {
        Auction createdAuction = auctionService.createAuction(auction);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuction);
    }

    // 更新拍卖信息
    @PutMapping("/updateAuction/{auctionId}")
    public ResponseEntity<Auction> updateAuction(@PathVariable Integer auctionId, @RequestBody Auction auction) {
        Auction updatedAuction = auctionService.updateAuction(auctionId, auction);
        return ResponseEntity.ok(updatedAuction);
    }
    //根据多个artworkId获取Auction列表
    @PostMapping("/getAuctionsByArtworkIds")
    public ResponseEntity<List<Auction>> getAuctionsByArtworkIds(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> artworkIds = request.get("artworkIds");
        List<Auction> auctions = auctionService.getAuctionsByArtworkIds(artworkIds);
        return ResponseEntity.ok(auctions);
    }
    // 删除拍卖
    @DeleteMapping("/deleteAuction/{auctionId}")
    public ResponseEntity<Void> deleteAuction(@PathVariable Integer auctionId) {
        auctionService.deleteAuction(auctionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/rejectAuction/{auctionId}")
    public ResponseEntity<Void> rejectAuction(@PathVariable Integer auctionId) {
        auctionService.rejectAuction(auctionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    //拍卖结算
    @PutMapping("/settleAuction/{auctionId}")
    public ResponseEntity<String> settleAuction(@PathVariable Integer auctionId) {
        try {
            boolean isSettled = auctionService.settleAuction(auctionId);
            if (isSettled) {
                return ResponseEntity.ok("拍卖已结算，订单已生成");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("拍卖尚未结束，无法结算");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("结算失败：" + e.getMessage());
        }
    }

}
