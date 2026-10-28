package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_events")
public class ShippingEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "shipping_id", nullable = false) private Integer shippingId;
    @Column(nullable = false, length = 500) private String description;
    @Column(name = "occurred_at", nullable = false) private LocalDateTime occurredAt;
    @PrePersist void onCreate() { occurredAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Integer getShippingId() { return shippingId; }
    public void setShippingId(Integer shippingId) { this.shippingId = shippingId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
