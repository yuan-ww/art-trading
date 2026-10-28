package com.example.demo.service;

import com.example.demo.model.Artwork;
import com.example.demo.model.Order;
import com.example.demo.model.User;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletTransactionRepository;
import com.example.demo.model.WalletTransaction;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final Duration PAYMENT_TIMEOUT = Duration.ofHours(24);
    private final OrderRepository orderRepository;
    private final ArtworksRepository artworksRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final NotificationService notificationService;
    public OrderService(
            OrderRepository orderRepository,
            ArtworksRepository artworksRepository,
            UserRepository userRepository,
            WalletTransactionRepository walletTransactionRepository,
            NotificationService notificationService
    ) {
        this.orderRepository = orderRepository;
        this.artworksRepository = artworksRepository;
        this.userRepository = userRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.notificationService = notificationService;
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
        order.initializeTimestamps();
        logger.debug("Creating new order for artworkId: {}", artworkId);
        return orderRepository.save(order);
    }
    public Page<Order> getOrdersPage(Integer buyerId, Integer sellerId, Pageable pageable) {
        if (buyerId != null) return orderRepository.findByBuyerId(buyerId, pageable);
        if (sellerId != null) return orderRepository.findBySellerId(sellerId, pageable);
        return orderRepository.findAll(pageable);
    }

    @Transactional
    public Order createDirectSaleOrder(Order order) {
        Integer artworkId = order.getArtworkId();
        Artwork artwork = artworksRepository.findByArtworkIdForUpdate(artworkId)
                .orElseThrow(() -> new RuntimeException("Artwork not found for artworkId: " + artworkId));
        if (!"直售".equals(artwork.getSaleType()) || artwork.getStatus() != Artwork.Status.在售) {
            throw new IllegalStateException("艺术品当前不能直接购买");
        }
        if (order.getBuyerId() == null || order.getBuyerId().equals(artwork.getUserId())) {
            throw new IllegalStateException("不能购买自己的艺术品");
        }
        if (artwork.getFixedPrice() == null || artwork.getFixedPrice().signum() <= 0) {
            throw new IllegalStateException("艺术品价格无效");
        }

        order.setOrderId(null);
        order.setVersion(null);
        order.setPaymentStatus(Order.PaymentStatus.Pending);
        order.setShippingStatus(Order.ShippingStatus.Pending);
        order.setPaymentRequestKey(null);
        order.setCreatedAt(null);
        order.setUpdatedAt(null);
        order.setSellerId(artwork.getUserId());
        order.setFinalPrice(artwork.getFixedPrice());
        artwork.setStatus(Artwork.Status.已售);
        artworksRepository.save(artwork);
        order.initializeTimestamps();
        logger.debug("Creating direct-sale order for artworkId: {}", artworkId);
        return orderRepository.save(order);
    }

    public void deleteOrder(Integer orderId) {
        logger.debug("Deleting order with ID: {}", orderId);
        orderRepository.deleteById(orderId);
    }
    @Transactional
    public Order payOrder(Integer buyerId, Integer artworkId) {
        return payOrder(buyerId, artworkId, null);
    }

    @Transactional
    public Order payOrder(Integer buyerId, Integer artworkId, String requestKey) {
        if (requestKey != null && !requestKey.isBlank()) {
            if (requestKey.length() > 80) throw new IllegalArgumentException("请求标识过长");
            Optional<Order> previous = orderRepository.findByPaymentRequestKey(requestKey);
            if (previous.isPresent()) {
                Order result = previous.get();
                if (!buyerId.equals(result.getBuyerId()) || !artworkId.equals(result.getArtworkId())) {
                    throw new IllegalArgumentException("请求标识已用于其他订单");
                }
                return result;
            }
        }
        Order order;
        if (requestKey != null && !requestKey.isBlank()) {
            List<Order> lockedOrders = orderRepository.findBuyerArtworkOrdersForUpdate(buyerId, artworkId);
            Optional<Order> retry = lockedOrders.stream().filter(o -> requestKey.equals(o.getPaymentRequestKey())).findFirst();
            if (retry.isPresent()) return retry.get();
            order = lockedOrders.stream().filter(o -> o.getPaymentStatus() == Order.PaymentStatus.Pending)
                    .findFirst().orElseThrow(() -> new IllegalStateException("未找到待支付订单"));
        } else {
            order = findLatestOrderForUpdate(buyerId, artworkId, Order.PaymentStatus.Pending);
        }
        if (requestKey != null && !requestKey.isBlank()) {
            order.setPaymentRequestKey(requestKey);
        }
        if (order.getFinalPrice() == null || order.getFinalPrice().signum() < 0) {
            throw new IllegalStateException("订单金额无效");
        }
        LockedParticipants participants = lockParticipants(order);
        User buyer = participants.buyer();
        User seller = participants.seller();
        if (buyer.getWalletBalance() == null || buyer.getWalletBalance().compareTo(order.getFinalPrice()) < 0) {
            throw new IllegalStateException("余额不足");
        }

        buyer.setWalletBalance(buyer.getWalletBalance().subtract(order.getFinalPrice()));
        BigDecimal sellerBalance = seller.getWalletBalance() == null ? BigDecimal.ZERO : seller.getWalletBalance();
        seller.setWalletBalance(sellerBalance.add(order.getFinalPrice()));
        order.setPaymentStatus(Order.PaymentStatus.Paid);
        userRepository.save(buyer);
        userRepository.save(seller);
        recordTransaction(buyer, order, order.getFinalPrice().negate(), WalletTransaction.Type.PAYMENT, "订单支付");
        recordTransaction(seller, order, order.getFinalPrice(), WalletTransaction.Type.SALE_INCOME, "订单收款");
        notificationService.send(order.getSellerId(), "订单 " + order.getOrderId() + " 已付款，请及时发货");
        return orderRepository.save(order);
    }

    @Transactional
    public Order refundOrder(Integer buyerId, Integer artworkId) {
        Order order = findLatestOrderForUpdate(buyerId, artworkId, Order.PaymentStatus.Paid);
        if (order.getShippingStatus() != Order.ShippingStatus.Pending) {
            throw new IllegalStateException("已发货订单需要走退货流程，不能直接退款");
        }
        if (order.getFinalPrice() == null || order.getFinalPrice().signum() < 0) {
            throw new IllegalStateException("订单金额无效");
        }
        LockedParticipants participants = lockParticipants(order);
        User buyer = participants.buyer();
        User seller = participants.seller();
        BigDecimal sellerBalance = seller.getWalletBalance() == null ? BigDecimal.ZERO : seller.getWalletBalance();
        if (sellerBalance.compareTo(order.getFinalPrice()) < 0) {
            throw new IllegalStateException("卖家余额不足，无法完成退款");
        }

        return completeRefund(order, buyer, seller, "订单退款");
    }

    @Transactional
    public Order refundReturnedOrder(Integer orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        if (order.getPaymentStatus() != Order.PaymentStatus.Paid || order.getShippingStatus() == Order.ShippingStatus.Pending) {
            throw new IllegalStateException("当前订单不能走退货退款");
        }
        LockedParticipants participants = lockParticipants(order);
        BigDecimal sellerBalance = participants.seller().getWalletBalance() == null ? BigDecimal.ZERO : participants.seller().getWalletBalance();
        if (sellerBalance.compareTo(order.getFinalPrice()) < 0) throw new IllegalStateException("卖家余额不足，无法退款");
        return completeRefund(order, participants.buyer(), participants.seller(), "退货退款");
    }

    private Order completeRefund(Order order, User buyer, User seller, String note) {
        BigDecimal price = order.getFinalPrice();
        BigDecimal buyerBalance = buyer.getWalletBalance() == null ? BigDecimal.ZERO : buyer.getWalletBalance();
        buyer.setWalletBalance(buyerBalance.add(price));
        BigDecimal sellerBalance = seller.getWalletBalance() == null ? BigDecimal.ZERO : seller.getWalletBalance();
        seller.setWalletBalance(sellerBalance.subtract(price));
        order.setPaymentStatus(Order.PaymentStatus.Refunded);
        userRepository.save(buyer); userRepository.save(seller);
        recordTransaction(buyer, order, price, WalletTransaction.Type.REFUND, note);
        recordTransaction(seller, order, price.negate(), WalletTransaction.Type.REFUND, note);
        notificationService.send(order.getBuyerId(), "订单 " + order.getOrderId() + " 已退款");
        return orderRepository.save(order);
    }

    private void recordTransaction(User user, Order order, BigDecimal amount, WalletTransaction.Type type, String note) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUserId(user.getUserId()); transaction.setOrderId(order.getOrderId()); transaction.setAmount(amount);
        transaction.setBalanceAfter(user.getWalletBalance()); transaction.setType(type); transaction.setNote(note);
        walletTransactionRepository.save(transaction);
    }

    @Transactional
    public boolean handleExpiredOrder(Integer orderId) {
        Optional<Order> orderOptional = orderRepository.findByIdForUpdate(orderId);
        if (orderOptional.isEmpty()) {
            return false; // 订单不存在
        }
        Order order = orderOptional.get();
        Timestamp createdAt = order.getCreatedAt();
        boolean isExpired = createdAt != null
                && !createdAt.toInstant().plus(PAYMENT_TIMEOUT).isAfter(java.time.Instant.now());
        if (order.getPaymentStatus() == Order.PaymentStatus.Pending && isExpired) {
            order.setPaymentStatus(Order.PaymentStatus.Cancel);
            orderRepository.save(order);
            Integer artworkId = order.getArtworkId();
            Optional<Artwork> artworkOptional = artworksRepository.findByArtworkIdForUpdate(artworkId);
            if (artworkOptional.isPresent()) {
                Artwork artwork = artworkOptional.get();
                artwork.setStatus(Artwork.Status.在售);
                artworksRepository.save(artwork);
            }
            return true;
        }
        return false; // 订单已经处理过
    }

    @Transactional
    public int expireTimedOutPendingOrders() {
        Timestamp deadline = Timestamp.from(java.time.Instant.now().minus(PAYMENT_TIMEOUT));
        int expiredCount = 0;
        for (Order order : orderRepository.findByPaymentStatusAndCreatedAtLessThanEqual(Order.PaymentStatus.Pending, deadline)) {
            if (handleExpiredOrder(order.getOrderId())) {
                expiredCount++;
            }
        }
        return expiredCount;
    }

    private Order findLatestOrderForUpdate(Integer buyerId, Integer artworkId, Order.PaymentStatus paymentStatus) {
        return orderRepository.findByBuyerIdAndArtworkIdAndPaymentStatusForUpdate(buyerId, artworkId, paymentStatus)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到可处理的订单"));
    }

    private LockedParticipants lockParticipants(Order order) {
        if (order.getBuyerId() == null || order.getSellerId() == null || order.getBuyerId().equals(order.getSellerId())) {
            throw new IllegalStateException("订单买卖双方无效");
        }
        Integer firstUserId = Math.min(order.getBuyerId(), order.getSellerId());
        Integer secondUserId = Math.max(order.getBuyerId(), order.getSellerId());
        User firstUser = userRepository.findByUserIdForUpdate(firstUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        User secondUser = userRepository.findByUserIdForUpdate(secondUserId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        User buyer = order.getBuyerId().equals(firstUserId) ? firstUser : secondUser;
        User seller = order.getSellerId().equals(firstUserId) ? firstUser : secondUser;
        return new LockedParticipants(buyer, seller);
    }

    private record LockedParticipants(User buyer, User seller) {
    }

}

