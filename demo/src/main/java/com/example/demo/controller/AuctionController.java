package com.example.demo.controller;

import com.example.demo.model.Auction;
import com.example.demo.model.Artwork;
import com.example.demo.repository.AuctionRegistrationRepository;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.service.AuctionRegistrationService;
import com.example.demo.service.AuctionService;
import com.example.demo.service.AccessControlService;
import com.example.demo.service.AuctionSettlementService.SettlementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;
    @Autowired
    private AuctionRegistrationService auctionRegistrationService;
    @Autowired
    private ArtworksRepository artworksRepository;
    @Autowired
    private AccessControlService accessControlService;

    // 获取所有拍卖
    @GetMapping("/getAuction")
    public ResponseEntity<List<Auction>> getAllAuctions() {
        List<Auction> auctions = auctionService.getAllAuctions();
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
        Integer artworkId = auction.getArtworkId();
        if (artworkId == null && auction.getArtwork() != null) {
            artworkId = auction.getArtwork().getArtworkId();
        }
        Artwork artwork = artworksRepository.findById(artworkId)
                .orElseThrow(() -> new IllegalArgumentException("艺术品不存在"));
        accessControlService.requireSelfOrAdmin(artwork.getUserId());
        Auction createdAuction = auctionService.createAuction(auction);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuction);
    }

    // 更新拍卖信息
    @PutMapping("/updateAuction/{auctionId}")
    public ResponseEntity<Auction> updateAuction(@PathVariable Integer auctionId, @RequestBody Auction auction) {
        Auction existing = auctionService.getAuctionById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("拍卖不存在"));
        Artwork artwork = artworksRepository.findById(existing.getArtworkId())
                .orElseThrow(() -> new IllegalArgumentException("艺术品不存在"));
        accessControlService.requireSelfOrAdmin(artwork.getUserId());
        if (!accessControlService.hasAdminRole()
                && (existing.getStatus() != Auction.AuctionStatus.审核中
                || auction.getStatus() != Auction.AuctionStatus.审核中)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
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
        Auction existing = auctionService.getAuctionById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("拍卖不存在"));
        Artwork artwork = artworksRepository.findById(existing.getArtworkId())
                .orElseThrow(() -> new IllegalArgumentException("艺术品不存在"));
        accessControlService.requireSelfOrAdmin(artwork.getUserId());
        if (!accessControlService.hasAdminRole() && existing.getStatus() != Auction.AuctionStatus.审核中) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        auctionService.deleteAuction(auctionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/rejectAuction/{auctionId}")
    public ResponseEntity<Void> rejectAuction(@PathVariable Integer auctionId) {
        accessControlService.requireAdmin();
        auctionService.rejectAuction(auctionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    @GetMapping("/page")
    public Page<Auction> getAuctionPage(@RequestParam(required = false) Auction.AuctionStatus status,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return auctionService.getAuctionPage(status, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("auctionId").descending()));
    }

    @PutMapping("/{auctionId}/resubmit")
    public ResponseEntity<Auction> resubmit(@PathVariable Integer auctionId, @RequestBody Auction details) {
        Auction existing = auctionService.getAuctionById(auctionId).orElseThrow(() -> new IllegalArgumentException("拍卖不存在"));
        Artwork artwork = artworksRepository.findById(existing.getArtworkId()).orElseThrow(() -> new IllegalArgumentException("艺术品不存在"));
        accessControlService.requireSelfOrAdmin(artwork.getUserId());
        return ResponseEntity.ok(auctionService.resubmitAuction(auctionId, details));
    }

    @PutMapping("/approveAuction/{auctionId}")
    public ResponseEntity<Auction> approveAuction(@PathVariable Integer auctionId) {
        accessControlService.requireAdmin();
        return ResponseEntity.ok(auctionService.approveAuction(auctionId));
    }
    //拍卖结算
    @PutMapping("/settleAuction/{auctionId}")
    public ResponseEntity<String> settleAuction(@PathVariable Integer auctionId) {
        try {
            accessControlService.requireAdmin();
            SettlementResult result = auctionService.settleAuction(auctionId);
            return switch (result) {
                case SETTLED -> ResponseEntity.ok("拍卖已结算，订单已生成");
                case UNSOLD -> ResponseEntity.ok("拍卖已流拍，未生成订单");
                case NOT_ENDED -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("拍卖尚未结束，无法结算");
                case ALREADY_PROCESSED -> ResponseEntity.status(HttpStatus.CONFLICT).body("拍卖已处理");
            };
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("拍卖不存在");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("结算失败");
        }
    }

}
