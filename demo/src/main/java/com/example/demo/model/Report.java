package com.example.demo.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name = "reports")
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "reporter_id", nullable = false) private Integer reporterId;
    @Column(name = "artwork_id", nullable = false) private Integer artworkId;
    @Column(nullable = false, length = 500) private String reason;
    @Column(nullable = false, length = 20) private String status = "Pending";
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "resolved_at") private LocalDateTime resolvedAt;
    @PrePersist void onCreate() { createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Integer getReporterId() { return reporterId; } public void setReporterId(Integer value) { reporterId = value; }
    public Integer getArtworkId() { return artworkId; } public void setArtworkId(Integer value) { artworkId = value; }
    public String getReason() { return reason; } public void setReason(String value) { reason = value; }
    public String getStatus() { return status; } public void setStatus(String value) { status = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; } public void setResolvedAt(LocalDateTime value) { resolvedAt = value; }
}
