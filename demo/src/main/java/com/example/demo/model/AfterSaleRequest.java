package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "after_sale_requests", uniqueConstraints = @UniqueConstraint(name = "uq_after_sale_order", columnNames = "order_id"))
public class AfterSaleRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long requestId;
    @Column(name = "order_id", nullable = false) private Integer orderId;
    @Column(name = "buyer_id", nullable = false) private Integer buyerId;
    @Column(nullable = false, length = 500) private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Status status = Status.Requested;
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "processed_at") private LocalDateTime processedAt;
    public enum Status { Requested, Rejected, Refunded }
    @PrePersist void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getRequestId(){return requestId;} public Integer getOrderId(){return orderId;} public void setOrderId(Integer v){orderId=v;} public Integer getBuyerId(){return buyerId;} public void setBuyerId(Integer v){buyerId=v;} public String getReason(){return reason;} public void setReason(String v){reason=v;} public Status getStatus(){return status;} public void setStatus(Status v){status=v;} public void setProcessedAt(LocalDateTime v){processedAt=v;}
}
