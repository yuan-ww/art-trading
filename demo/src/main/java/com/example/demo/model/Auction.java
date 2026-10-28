package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
@Data
@Entity
@Table(name = "Auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auction_id")
    private Integer auctionId;  // 拍卖记录的唯一ID

    @Column(name = "artwork_id")
    private Integer artworkId;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artwork_id", nullable = false,insertable = false, updatable = false)
    private Artwork artwork;  // 关联的艺术品

    @Column(name = "starting_price", nullable = false)
    private BigDecimal startingPrice;  // 起始价格

    @Column(name = "deposit_percentage" )
    private BigDecimal depositPercentage;  // 保证金百分比

    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;  // 实际保证金金额

    @Column(name = "current_highest_bid", nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private BigDecimal currentHighestBid = BigDecimal.ZERO;  // 当前最高出价，默认为0

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"email", "password", "phone", "realName", "idCardNumber", "address", "walletBalance", "role"})
    @JoinColumn(name = "highest_bidder_id")
    private User highestBidder;  // 当前最高出价者

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @PrePersist
    public void beforePersist() {
        if (startTime != null) {
            this.startTime = startTime.atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        }
        if (endTime != null) {
            this.endTime = endTime.atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuctionStatus status = AuctionStatus.审核中;  // 拍卖状态
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public enum AuctionStatus {
        审核中,
        进行中,
        已结束,
        流拍,
        已取消;
    }

    // Getters and setters
    public Integer getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Integer auctionId) {
        this.auctionId = auctionId;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getDepositPercentage() {
        return depositPercentage;
    }

    public void setDepositPercentage(BigDecimal depositPercentage) {
        this.depositPercentage = depositPercentage;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public User getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(User highestBidder) {
        this.highestBidder = highestBidder;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public Integer getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(Integer artworkId) {
        this.artworkId = artworkId;
    }
}
