package com.example.demo.dto;

import com.example.demo.model.Order;
import java.math.BigDecimal;
import java.sql.Timestamp;

public record OrderListItem(Integer orderId, Integer artworkId, String artworkName,
                            String buyerName, String sellerName, BigDecimal finalPrice,
                            Timestamp createdAt, Order.PaymentStatus paymentStatus,
                            Order.ShippingStatus shippingStatus) { }
