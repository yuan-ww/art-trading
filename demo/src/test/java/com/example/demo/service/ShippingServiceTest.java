package com.example.demo.service;

import com.example.demo.model.Order;
import com.example.demo.model.Shipping;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ShippingRepository;
import com.example.demo.repository.ShippingEventRepository;
import com.example.demo.repository.AfterSaleRequestRepository;
import com.example.demo.model.AfterSaleRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {
    @Mock
    private ShippingRepository shippingRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ShippingEventRepository shippingEventRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AfterSaleRequestRepository afterSaleRequests;
    @InjectMocks
    private ShippingService shippingService;

    @Test
    void requestKeyCannotExposeShipmentFromAnotherOrder() {
        Shipping request = shipment();
        request.setRequestKey("shared-key");
        Shipping existing = shipment();
        existing.setOrderId(99);
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(paidOrder()));
        when(shippingRepository.findByRequestKey("shared-key")).thenReturn(existing);
        assertThrows(IllegalArgumentException.class, () -> shippingService.createShipping(request));
    }

    @Test
    void paidOrderCreatesOneServerControlledShipment() {
        Order order = paidOrder();
        Shipping shipping = shipment();
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));
        when(shippingRepository.existsByOrderId(1)).thenReturn(false);
        when(shippingRepository.save(any(Shipping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Shipping created = shippingService.createShipping(shipping);

        assertEquals(4, created.getSenderId());
        assertEquals(5, created.getReceiverId());
        assertEquals(Shipping.ShippingStatus.Shipped, created.getShippingStatus());
        assertEquals(Order.ShippingStatus.Shipped, order.getShippingStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void unpaidOrderCannotCreateShipment() {
        Order order = paidOrder();
        order.setPaymentStatus(Order.PaymentStatus.Pending);
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> shippingService.createShipping(shipment()));
    }

    @Test
    void buyerCanConfirmOnlyAShippedPaidOrder() {
        Order order = paidOrder();
        order.setShippingStatus(Order.ShippingStatus.Shipped);
        Shipping shipping = shipment();
        shipping.setShippingStatus(Shipping.ShippingStatus.Shipped);
        when(shippingRepository.findById(2)).thenReturn(Optional.of(shipping));
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));

        shippingService.updateShippingStatus(2, Shipping.ShippingStatus.Delivered);

        assertEquals(Shipping.ShippingStatus.Delivered, shipping.getShippingStatus());
        assertEquals(Order.ShippingStatus.Delivered, order.getShippingStatus());
        verify(shippingRepository).save(shipping);
        verify(orderRepository).save(order);
    }

    @Test
    void deletingUndeliveredShipmentResetsOrderToPending() {
        Order order = paidOrder();
        order.setShippingStatus(Order.ShippingStatus.Shipped);
        Shipping shipping = shipment();
        shipping.setShippingId(2);
        shipping.setShippingStatus(Shipping.ShippingStatus.Shipped);
        when(shippingRepository.findById(2)).thenReturn(Optional.of(shipping));
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));

        boolean deleted = shippingService.deleteShipping(2);

        assertEquals(true, deleted);
        assertEquals(Order.ShippingStatus.Pending, order.getShippingStatus());
        verify(orderRepository).save(order);
        verify(shippingRepository).delete(shipping);
    }

    @Test
    void shipmentCannotBeDeletedWhileReturnRequestIsPending() {
        Order order = paidOrder();
        order.setShippingStatus(Order.ShippingStatus.Shipped);
        Shipping shipping = shipment(); shipping.setShippingId(2); shipping.setShippingStatus(Shipping.ShippingStatus.Shipped);
        AfterSaleRequest request = new AfterSaleRequest(); request.setStatus(AfterSaleRequest.Status.Requested);
        when(shippingRepository.findById(2)).thenReturn(Optional.of(shipping));
        when(orderRepository.findByIdForUpdate(1)).thenReturn(Optional.of(order));
        when(afterSaleRequests.findByOrderId(1)).thenReturn(Optional.of(request));

        assertThrows(IllegalStateException.class, () -> shippingService.deleteShipping(2));
    }

    private Order paidOrder() {
        Order order = new Order();
        order.setOrderId(1);
        order.setBuyerId(5);
        order.setSellerId(4);
        order.setPaymentStatus(Order.PaymentStatus.Paid);
        order.setShippingStatus(Order.ShippingStatus.Pending);
        return order;
    }

    private Shipping shipment() {
        Shipping shipping = new Shipping();
        shipping.setOrderId(1);
        shipping.setCarrier("物流公司");
        shipping.setTrackingNumber("TRACK-001");
        return shipping;
    }
}
