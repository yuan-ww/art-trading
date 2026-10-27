package com.example.demo.service;

import com.example.demo.model.Artwork;
import com.example.demo.model.Order;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final ArtworksRepository artworksRepository;
    public OrderService(OrderRepository orderRepository,ArtworksRepository artworksRepository) {
        this.orderRepository = orderRepository;
        this.artworksRepository = artworksRepository;
    }

    public List<Order> getAllOrders() {
        logger.debug("Fetching all orders");
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Integer orderId) {
        logger.debug("Fetching order with ID: {}", orderId);
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersByArtworkId(Integer artworkId) {
        logger.debug("Fetching orders with auctionId: {}", artworkId);
        return orderRepository.findByArtworkId(artworkId);
    }

    public List<Order> getOrdersByBuyerId(Integer buyerId) {
        logger.debug("Fetching orders for buyerId: {}", buyerId);
        return orderRepository.findByBuyerId(buyerId);
    }

    public List<Order> getOrdersBySellerId(Integer sellerId) {
        logger.debug("Fetching orders for sellerId: {}", sellerId);
        return orderRepository.findBySellerId(sellerId);
    }
@Transactional
    public Order createOrder(Order order) {
        Integer artworkId = order.getArtworkId(); // 获取 order 中的 artworkId
        Optional<Artwork> artworkOptional = artworksRepository.findByArtworkId(artworkId);
        if (artworkOptional.isPresent()) {
            Artwork artwork = artworkOptional.get();
            artwork.setStatus(Artwork.Status.已售);  // 更新艺术品状态为 "已售"
            artworksRepository.save(artwork);  // 保存更新后的艺术品
        } else {
            throw new RuntimeException("Artwork not found for artworkId: " + artworkId);
        }
        logger.debug("Creating new order: {}", order);
        return orderRepository.save(order);
    }

    public Order updateOrder(Integer orderId, Order orderDetails) {
        logger.debug("Updating order with ID: {}", orderId);
        return orderRepository.findById(orderId).map(order -> {
            order.setFinalPrice(orderDetails.getFinalPrice());
            order.setPaymentStatus(orderDetails.getPaymentStatus());
            order.setShippingStatus(orderDetails.getShippingStatus());
            logger.debug("Updated order details: {}", order);
            return orderRepository.save(order);
        }).orElseThrow(() -> {
            logger.error("Order not found with ID: {}", orderId);
            return new RuntimeException("Order not found");
        });
    }

    public void deleteOrder(Integer orderId) {
        logger.debug("Deleting order with ID: {}", orderId);
        orderRepository.deleteById(orderId);
    }
    public Order updateOrderStatus(Integer userId, Integer artworkId, Order.PaymentStatus status) {
        Optional<Order> orderOptional = orderRepository.findByBuyerIdAndArtworkId(userId, artworkId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setPaymentStatus(status);  // 更新订单的支付状态
            return orderRepository.save(order);  // 保存更新后的订单
        } else {
            throw new RuntimeException("Order not found for userId: " + userId + " and artworkId: " + artworkId);
        }
    }

    @Transactional
    public boolean handleExpiredOrder(Integer orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            return false; // 订单不存在
        }
        Order order = orderOptional.get();
        if (order.getPaymentStatus() == Order.PaymentStatus.Pending) {
            order.setPaymentStatus(Order.PaymentStatus.Cancel);
            orderRepository.save(order);
            Integer artworkId = order.getArtworkId();
            Optional<Artwork> artworkOptional = artworksRepository.findByArtworkId(artworkId);
            if (artworkOptional.isPresent()) {
                Artwork artwork = artworkOptional.get();
                artwork.setStatus(Artwork.Status.在售);
                artworksRepository.save(artwork);
            }
            return true;
        }
        return false; // 订单已经处理过
    }

}

