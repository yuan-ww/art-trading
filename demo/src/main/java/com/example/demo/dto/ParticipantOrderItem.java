package com.example.demo.dto;

import com.example.demo.model.Order;
import java.math.BigDecimal;
import java.sql.Timestamp;

public record ParticipantOrderItem(Integer orderId, Integer artworkId, Integer buyerId, Integer sellerId,
                                   String artworkName, String artworkAuthor, String imageUrl,
                                   BigDecimal finalPrice, Timestamp createdAt,
                                   Order.PaymentStatus paymentStatus, Order.ShippingStatus shippingStatus) { }
