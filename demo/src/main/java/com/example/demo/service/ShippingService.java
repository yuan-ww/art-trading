package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.Shipping;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ShippingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;
    @Autowired
    public ShippingService(ShippingRepository shippingRepository,OrderRepository orderRepository) {
        this.shippingRepository = shippingRepository;
        this.orderRepository = orderRepository;
    }

    // 根据订单ID获取物流信息
    public Shipping getShippingByOrderId(int orderId) {
        return shippingRepository.findByOrderId(orderId);
    }
    public Iterable<Shipping> getAllShipping() {
        return shippingRepository.findAll();  // 使用 repository 获取所有记录
    }
    // 更新物流信息
    public Shipping updateShipping(int shippingId, Shipping shippingDetails) {
        Shipping existingShipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("Shipping not found with ID: " + shippingId));

        existingShipping.setTrackingNumber(shippingDetails.getTrackingNumber());
        existingShipping.setCarrier(shippingDetails.getCarrier());
        existingShipping.setShippingStatus(shippingDetails.getShippingStatus());
        existingShipping.setSenderId(shippingDetails.getSenderId());
        existingShipping.setReceiverId(shippingDetails.getReceiverId());
        existingShipping.setSenderAddress(shippingDetails.getSenderAddress());
        existingShipping.setReceiverAddress(shippingDetails.getReceiverAddress());
        existingShipping.setSenderPhone(shippingDetails.getSenderPhone());
        existingShipping.setReceiverPhone(shippingDetails.getReceiverPhone());

        return shippingRepository.save(existingShipping);
    }
    // 创建新的物流信息
    public Shipping createShipping(Shipping shipping) {
        Optional<Order> optionalOrder = orderRepository.findById(shipping.getOrderId());
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setShippingStatus(Order.ShippingStatus.Shipped);  // 更新订单的物流状态
            orderRepository.save(order);
        } else {
            throw new RuntimeException("订单未找到，ID: " + shipping.getOrderId());
        }
        return shippingRepository.save(shipping);
    }
    public boolean deleteShipping(Integer shippingId) {
        if (shippingRepository.existsById(shippingId)) {
            shippingRepository.deleteById(shippingId);
            return true;
        }
        return false;
    }

    @Transactional // 启用事务管理，保证两个表一起更新
    public void updateShippingStatus(Integer shippingId, Shipping.ShippingStatus newStatus) {

        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("物流信息未找到"));
        shipping.setShippingStatus(newStatus);
        shippingRepository.save(shipping);
        if (newStatus == Shipping.ShippingStatus.Delivered) {
            Order order = orderRepository.findById(shipping.getOrderId())
                    .orElseThrow(() -> new RuntimeException("订单信息未找到"));

            order.setShippingStatus(Order.ShippingStatus.Delivered);
            orderRepository.save(order);
        }
    }
}
