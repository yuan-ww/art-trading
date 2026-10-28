package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_wallet_transactions_user_created", columnList = "user_id,created_at"),
        @Index(name = "idx_wallet_transactions_order", columnList = "order_id")
})
public class WalletTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    @Column(name = "user_id", nullable = false) private Integer userId;
    @Column(name = "order_id") private Integer orderId;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Type type;
    @Column(name = "balance_after", nullable = false, precision = 10, scale = 2) private BigDecimal balanceAfter;
    @Column(length = 255) private String note;
    @Column(name = "request_key", unique = true, length = 80) private String requestKey;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    public enum Type { TOP_UP, PAYMENT, SALE_INCOME, REFUND }
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getTransactionId() { return transactionId; } public Integer getUserId() { return userId; } public void setUserId(Integer v) { userId=v; }
    public Integer getOrderId() { return orderId; } public void setOrderId(Integer v) { orderId=v; } public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal v) { amount=v; }
    public Type getType() { return type; } public void setType(Type v) { type=v; } public BigDecimal getBalanceAfter() { return balanceAfter; } public void setBalanceAfter(BigDecimal v) { balanceAfter=v; }
    public String getNote() { return note; } public void setNote(String v) { note=v; } public LocalDateTime getCreatedAt() { return createdAt; }
    public String getRequestKey() { return requestKey; } public void setRequestKey(String v) { requestKey=v; }
}
