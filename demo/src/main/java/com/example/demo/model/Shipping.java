package com.example.demo.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(
        name = "Shipping",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_shipping_order",
                columnNames = "order_id"
        )
)
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shipping_id")
    private int shippingId;

    @Column(name = "order_id")
    private int orderId;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "carrier")
    private String carrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_status", columnDefinition = "ENUM('Pending', 'Shipped', 'Delivered') DEFAULT 'Pending'")
    private ShippingStatus shippingStatus = ShippingStatus.Pending;

    @Column(name = "shipping_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp shippingTime;

    @Column(name = "sender_id")
    private int senderId; // 发货人ID

    @Column(name = "receiver_id")
    private int receiverId; // 收货人ID

    @Column(name = "sender_address")
    private String senderAddress; // 发货人地址

    @Column(name = "receiver_address")
    private String receiverAddress; // 收货人地址

    @Column(name = "sender_phone")
    private String senderPhone; // 发货人电话

    @Column(name = "receiver_phone")
    private String receiverPhone; // 收货人电话
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    @Column(name = "request_key", unique = true, length = 80)
    private String requestKey;

    public enum ShippingStatus {
        Pending, Shipped, Delivered
    }

    // Getters and Setters

    public int getShippingId() {
        return shippingId;
    }

    public void setShippingId(int shippingId) {
        this.shippingId = shippingId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public ShippingStatus getShippingStatus() {
        return shippingStatus;
    }

    public void setShippingStatus(ShippingStatus shippingStatus) {
        this.shippingStatus = shippingStatus;
    }

    public Timestamp getShippingTime() {
        return shippingTime;
    }

    public void setShippingTime(Timestamp shippingTime) {
        this.shippingTime = shippingTime;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getRequestKey() { return requestKey; }

    public void setRequestKey(String requestKey) { this.requestKey = requestKey; }
}
