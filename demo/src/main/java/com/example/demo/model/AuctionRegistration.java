package com.example.demo.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "auctionregistrations",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_auction_registrations_auction_user",
                columnNames = {"auction_id", "user_id"}
        )
)
public class AuctionRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer registrationId;

    @ManyToOne
    @JoinColumn(name = "auction_id", referencedColumnName = "auction_id")
    private Auction auction;

    @ManyToOne
    @JsonIgnoreProperties({"email", "password", "phone", "realName", "idCardNumber", "address", "walletBalance", "role"})
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "deposit_paid", nullable = false)
    private Boolean depositPaid = false;

    @Column(name = "deposit_amount", nullable = false)
    private BigDecimal depositAmount;

    @Column(name = "registration_time", nullable = false)
    private LocalDateTime registrationTime = LocalDateTime.now();

    // Getters and Setters
    public Integer getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Integer registrationId) {
        this.registrationId = registrationId;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getDepositPaid() {
        return depositPaid;
    }

    public void setDepositPaid(Boolean depositPaid) {
        this.depositPaid = depositPaid;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(BigDecimal depositAmount) {
        this.depositAmount = depositAmount;
    }

    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(LocalDateTime registrationTime) {
        this.registrationTime = registrationTime;
    }
}
