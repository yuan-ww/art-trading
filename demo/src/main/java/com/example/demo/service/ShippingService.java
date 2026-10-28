package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.Shipping;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ShippingRepository;
import com.example.demo.repository.ShippingEventRepository;
import com.example.demo.repository.AfterSaleRequestRepository;
import com.example.demo.model.AfterSaleRequest;
import com.example.demo.model.ShippingEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;
    private final ShippingEventRepository shippingEventRepository;
    private final NotificationService notificationService;
    private final AfterSaleRequestRepository afterSaleRequests;
    @Autowired
    public ShippingService(ShippingRepository shippingRepository,OrderRepository orderRepository,
                           ShippingEventRepository shippingEventRepository, NotificationService notificationService,
                           AfterSaleRequestRepository afterSaleRequests) {
        this.shippingRepository = shippingRepository;
        this.orderRepository = orderRepository;
        this.shippingEventRepository = shippingEventRepository;
        this.notificationService = notificationService;
        this.afterSaleRequests = afterSaleRequests;
    }

    // 根据订单ID获取物流信息
    public Shipping getShippingByOrderId(int orderId) {
        return shippingRepository.findByOrderId(orderId);
    }
    public Optional<Shipping> getShippingById(Integer shippingId) {
        return shippingRepository.findById(shippingId);
    }
    public Iterable<Shipping> getAllShipping() {
        return shippingRepository.findAll();  // 使用 repository 获取所有记录
    }
    public Page<Shipping> getShippingPage(Shipping.ShippingStatus status, Pageable pageable) {
        return status == null ? shippingRepository.findAll(pageable) : shippingRepository.findByShippingStatus(status, pageable);
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
    @Transactional
    public Shipping createShipping(Shipping shipping) {
        Order order = orderRepository.findByIdForUpdate(shipping.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("订单未找到"));
        if (shipping.getRequestKey() != null && !shipping.getRequestKey().isBlank()) {
            if (shipping.getRequestKey().length() > 80) throw new IllegalArgumentException("请求标识过长");
            Shipping existing = shippingRepository.findByRequestKey(shipping.getRequestKey());
            if (existing != null) {
                if (existing.getOrderId() != shipping.getOrderId()
                        || !java.util.Objects.equals(existing.getCarrier(), shipping.getCarrier())
                        || !java.util.Objects.equals(existing.getTrackingNumber(), shipping.getTrackingNumber())
                        || !java.util.Objects.equals(existing.getSenderAddress(), shipping.getSenderAddress())
                        || !java.util.Objects.equals(existing.getReceiverAddress(), shipping.getReceiverAddress())
                        || !java.util.Objects.equals(existing.getSenderPhone(), shipping.getSenderPhone())
                        || !java.util.Objects.equals(existing.getReceiverPhone(), shipping.getReceiverPhone())) {
                    throw new IllegalArgumentException("请求标识已用于不同的物流请求");
                }
                return existing;
            }
        }
        if (order.getPaymentStatus() != Order.PaymentStatus.Paid) {
            throw new IllegalStateException("订单未支付，不能创建物流");
        }
        if (order.getShippingStatus() != Order.ShippingStatus.Pending || shippingRepository.existsByOrderId(shipping.getOrderId())) {
            throw new IllegalStateException("订单已经发货，不能重复创建物流");
        }
        if (shipping.getCarrier() == null || shipping.getCarrier().trim().isEmpty()
                || shipping.getTrackingNumber() == null || shipping.getTrackingNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("物流公司和运单号不能为空");
        }

        shipping.setSenderId(order.getSellerId());
        shipping.setReceiverId(order.getBuyerId());
        shipping.setShippingStatus(Shipping.ShippingStatus.Shipped);
        order.setShippingStatus(Order.ShippingStatus.Shipped);
        orderRepository.save(order);
        Shipping saved = shippingRepository.save(shipping);
        addEvent(saved.getShippingId(), "卖家已发货");
        notificationService.send(order.getBuyerId(), "订单 " + order.getOrderId() + " 已发货");
        return saved;
    }
    @Transactional
    public boolean deleteShipping(Integer shippingId) {
        Optional<Shipping> shippingOptional = shippingRepository.findById(shippingId);
        if (shippingOptional.isEmpty()) {
            return false;
        }
        Shipping shipping = shippingOptional.get();
        Order order = orderRepository.findByIdForUpdate(shipping.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("订单信息未找到"));
        if (shipping.getShippingStatus() == Shipping.ShippingStatus.Delivered
                || order.getShippingStatus() == Order.ShippingStatus.Delivered) {
            throw new IllegalStateException("已送达物流不能删除");
        }
        if (afterSaleRequests != null && afterSaleRequests.findByOrderId(order.getOrderId())
                .map(request -> request.getStatus() == AfterSaleRequest.Status.Requested).orElse(false)) {
            throw new IllegalStateException("售后处理中不能删除物流");
        }
        order.setShippingStatus(Order.ShippingStatus.Pending);
        orderRepository.save(order);
        shippingEventRepository.deleteByShippingId(shippingId);
        shippingRepository.delete(shipping);
        return true;
    }

    @Transactional // 启用事务管理，保证两个表一起更新
    public void updateShippingStatus(Integer shippingId, Shipping.ShippingStatus newStatus) {

        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new RuntimeException("物流信息未找到"));
        if (newStatus != Shipping.ShippingStatus.Delivered) {
            throw new IllegalArgumentException("物流状态只能由买家确认收货");
        }
        if (shipping.getShippingStatus() == Shipping.ShippingStatus.Delivered
                && orderRepository.findByIdForUpdate(shipping.getOrderId())
                .map(order -> order.getShippingStatus() == Order.ShippingStatus.Delivered).orElse(false)) {
            return;
        }
        if (shipping.getShippingStatus() != Shipping.ShippingStatus.Shipped) {
            throw new IllegalStateException("当前物流状态不能确认收货");
        }
        Order order = orderRepository.findByIdForUpdate(shipping.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("订单信息未找到"));
        if (order.getPaymentStatus() != Order.PaymentStatus.Paid) {
            throw new IllegalStateException("未支付订单不能确认收货");
        }
        shipping.setShippingStatus(Shipping.ShippingStatus.Delivered);
        shippingRepository.save(shipping);
        order.setShippingStatus(Order.ShippingStatus.Delivered);
        orderRepository.save(order);
        addEvent(shippingId, "买家已确认收货");
        notificationService.send(order.getSellerId(), "订单 " + order.getOrderId() + " 已确认收货");
    }

    private void addEvent(Integer shippingId, String description) {
        ShippingEvent event = new ShippingEvent(); event.setShippingId(shippingId); event.setDescription(description);
        shippingEventRepository.save(event);
    }
}
