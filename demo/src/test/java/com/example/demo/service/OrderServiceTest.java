package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.Artwork;
import com.example.demo.model.User;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ArtworksRepository artworksRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private OrderService orderService;

    @Test
    void retryReturnsPreviousPaymentAndRejectsKeyReuseForAnotherBuyer() {
        Order order = new Order();
        order.setBuyerId(5);
        order.setArtworkId(8);
        order.setPaymentStatus(Order.PaymentStatus.Paid);
        when(orderRepository.findByPaymentRequestKey("retry-1")).thenReturn(Optional.of(order));
        assertEquals(order, orderService.payOrder(5, 8, "retry-1"));
        assertThrows(IllegalArgumentException.class, () -> orderService.payOrder(6, 8, "retry-1"));
    }

    @Test
    void payingOrderDebitsWalletAndMarksOrderPaidTogether() {
        Order order = new Order();
        order.setBuyerId(5);
        order.setArtworkId(8);
        order.setSellerId(4);
        order.setFinalPrice(new BigDecimal("25.00"));
        order.setPaymentStatus(Order.PaymentStatus.Pending);
        User buyer = new User();
        buyer.setUserId(5);
        buyer.setWalletBalance(new BigDecimal("40.00"));
        User seller = new User();
        seller.setUserId(4);
        seller.setWalletBalance(new BigDecimal("20.00"));

        when(orderRepository.findByBuyerIdAndArtworkIdAndPaymentStatusForUpdate(
                5, 8, Order.PaymentStatus.Pending)).thenReturn(List.of(order));
        when(userRepository.findByUserIdForUpdate(5)).thenReturn(Optional.of(buyer));
        when(userRepository.findByUserIdForUpdate(4)).thenReturn(Optional.of(seller));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order paidOrder = orderService.payOrder(5, 8);

        assertEquals(Order.PaymentStatus.Paid, paidOrder.getPaymentStatus());
        assertEquals(new BigDecimal("15.00"), buyer.getWalletBalance());
        assertEquals(new BigDecimal("45.00"), seller.getWalletBalance());
        verify(userRepository).save(buyer);
        verify(userRepository).save(seller);
        verify(orderRepository).save(order);
    }

    @Test
    void payingOrderRejectsInsufficientBalanceWithoutChangingStatus() {
        Order order = new Order();
        order.setBuyerId(5);
        order.setArtworkId(8);
        order.setSellerId(4);
        order.setFinalPrice(new BigDecimal("25.00"));
        order.setPaymentStatus(Order.PaymentStatus.Pending);
        User buyer = new User();
        buyer.setUserId(5);
        buyer.setWalletBalance(new BigDecimal("10.00"));
        User seller = new User();
        seller.setUserId(4);
        seller.setWalletBalance(new BigDecimal("20.00"));

        when(orderRepository.findByBuyerIdAndArtworkIdAndPaymentStatusForUpdate(
                5, 8, Order.PaymentStatus.Pending)).thenReturn(List.of(order));
        when(userRepository.findByUserIdForUpdate(5)).thenReturn(Optional.of(buyer));
        when(userRepository.findByUserIdForUpdate(4)).thenReturn(Optional.of(seller));

        assertThrows(IllegalStateException.class, () -> orderService.payOrder(5, 8));
        assertEquals(Order.PaymentStatus.Pending, order.getPaymentStatus());
        assertEquals(new BigDecimal("10.00"), buyer.getWalletBalance());
        assertEquals(new BigDecimal("20.00"), seller.getWalletBalance());
    }

    @Test
    void directSaleLocksArtworkAndUsesServerSidePriceAndSeller() {
        Artwork artwork = new Artwork();
        artwork.setArtworkId(8);
        artwork.setUserId(4);
        artwork.setSaleType("直售");
        artwork.setStatus(Artwork.Status.在售);
        artwork.setFixedPrice(new BigDecimal("25.00"));
        Order requestedOrder = new Order();
        requestedOrder.setArtworkId(8);
        requestedOrder.setBuyerId(5);
        requestedOrder.setSellerId(999);
        requestedOrder.setFinalPrice(new BigDecimal("0.01"));
        requestedOrder.setOrderId(999);
        requestedOrder.setPaymentStatus(Order.PaymentStatus.Paid);
        requestedOrder.setShippingStatus(Order.ShippingStatus.Delivered);
        requestedOrder.setPaymentRequestKey("forged");
        when(artworksRepository.findByArtworkIdForUpdate(8)).thenReturn(Optional.of(artwork));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order createdOrder = orderService.createDirectSaleOrder(requestedOrder);

        assertEquals(Artwork.Status.已售, artwork.getStatus());
        assertEquals(4, createdOrder.getSellerId());
        assertEquals(new BigDecimal("25.00"), createdOrder.getFinalPrice());
        assertEquals(null, createdOrder.getOrderId());
        assertEquals(Order.PaymentStatus.Pending, createdOrder.getPaymentStatus());
        assertEquals(Order.ShippingStatus.Pending, createdOrder.getShippingStatus());
        assertEquals(null, createdOrder.getPaymentRequestKey());
        verify(artworksRepository).save(artwork);
    }

    @Test
    void buyerCannotCreateDirectSaleOrderForOwnArtwork() {
        Artwork artwork = new Artwork();
        artwork.setArtworkId(8);
        artwork.setUserId(5);
        artwork.setSaleType("直售");
        artwork.setStatus(Artwork.Status.在售);
        Order requestedOrder = new Order();
        requestedOrder.setArtworkId(8);
        requestedOrder.setBuyerId(5);
        when(artworksRepository.findByArtworkIdForUpdate(8)).thenReturn(Optional.of(artwork));

        assertThrows(IllegalStateException.class, () -> orderService.createDirectSaleOrder(requestedOrder));
    }

    @Test
    void onlyExpiredPendingOrderCanBeCancelledAndRelisted() {
        Order order = new Order();
        order.setOrderId(1);
        order.setArtworkId(8);
        order.setPaymentStatus(Order.PaymentStatus.Pending);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis() - Duration.ofHours(25).toMillis()));
        Artwork artwork = new Artwork();
        artwork.setStatus(Artwork.Status.已售);
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));
        when(artworksRepository.findByArtworkIdForUpdate(8)).thenReturn(Optional.of(artwork));

        boolean cancelled = orderService.handleExpiredOrder(1);

        assertEquals(true, cancelled);
        assertEquals(Order.PaymentStatus.Cancel, order.getPaymentStatus());
        assertEquals(Artwork.Status.在售, artwork.getStatus());
        verify(orderRepository).save(order);
        verify(artworksRepository).save(artwork);
    }

    @Test
    void freshPendingOrderCannotBeCancelledAsExpired() {
        Order order = new Order();
        order.setOrderId(1);
        order.setPaymentStatus(Order.PaymentStatus.Pending);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis() - Duration.ofHours(23).toMillis()));
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));

        boolean cancelled = orderService.handleExpiredOrder(1);

        assertEquals(false, cancelled);
        assertEquals(Order.PaymentStatus.Pending, order.getPaymentStatus());
    }

    @Test
    void scheduledExpiryRechecksAndCancelsTimedOutPendingOrders() {
        Order order = new Order();
        order.setOrderId(1);
        order.setArtworkId(8);
        order.setPaymentStatus(Order.PaymentStatus.Pending);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis() - Duration.ofHours(25).toMillis()));
        Artwork artwork = new Artwork();
        artwork.setStatus(Artwork.Status.已售);
        when(orderRepository.findByPaymentStatusAndCreatedAtLessThanEqual(
                org.mockito.ArgumentMatchers.eq(Order.PaymentStatus.Pending), any(Timestamp.class)))
                .thenReturn(List.of(order));
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));
        when(artworksRepository.findByArtworkIdForUpdate(8)).thenReturn(Optional.of(artwork));

        int expiredCount = orderService.expireTimedOutPendingOrders();

        assertEquals(1, expiredCount);
        assertEquals(Order.PaymentStatus.Cancel, order.getPaymentStatus());
    }

    @Test
    void adminRefundReversesWalletTransferBeforeShipment() {
        Order order = new Order();
        order.setBuyerId(5);
        order.setSellerId(4);
        order.setArtworkId(8);
        order.setFinalPrice(new BigDecimal("25.00"));
        order.setPaymentStatus(Order.PaymentStatus.Paid);
        order.setShippingStatus(Order.ShippingStatus.Pending);
        User buyer = new User();
        buyer.setUserId(5);
        buyer.setWalletBalance(new BigDecimal("15.00"));
        User seller = new User();
        seller.setUserId(4);
        seller.setWalletBalance(new BigDecimal("45.00"));
        when(orderRepository.findByBuyerIdAndArtworkIdAndPaymentStatusForUpdate(
                5, 8, Order.PaymentStatus.Paid)).thenReturn(List.of(order));
        when(userRepository.findByUserIdForUpdate(4)).thenReturn(Optional.of(seller));
        when(userRepository.findByUserIdForUpdate(5)).thenReturn(Optional.of(buyer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order refunded = orderService.refundOrder(5, 8);

        assertEquals(Order.PaymentStatus.Refunded, refunded.getPaymentStatus());
        assertEquals(new BigDecimal("40.00"), buyer.getWalletBalance());
        assertEquals(new BigDecimal("20.00"), seller.getWalletBalance());
        verify(userRepository).save(buyer);
        verify(userRepository).save(seller);
    }
}
