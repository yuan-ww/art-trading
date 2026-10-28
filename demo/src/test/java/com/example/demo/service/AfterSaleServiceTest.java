package com.example.demo.service;

import com.example.demo.model.AfterSaleRequest;
import com.example.demo.model.Order;
import com.example.demo.repository.AfterSaleRequestRepository;
import com.example.demo.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AfterSaleServiceTest {
    @Mock AfterSaleRequestRepository requests;
    @Mock OrderRepository orders;
    @Mock OrderService orderService;
    @Mock NotificationService notifications;
    @InjectMocks AfterSaleService service;

    @Test void cannotRequestReturnBeforeShipment() {
        Order order = new Order(); order.setBuyerId(5); order.setPaymentStatus(Order.PaymentStatus.Paid);
        order.setShippingStatus(Order.ShippingStatus.Pending);
        when(orders.findByIdForUpdate(2)).thenReturn(Optional.of(order));
        assertThrows(IllegalStateException.class, () -> service.request(2, 5, "原因"));
        verify(requests, never()).save(any());
    }

    @Test void confirmationLocksRequestAndDoesNotRepeatRefund() {
        AfterSaleRequest request = new AfterSaleRequest(); request.setOrderId(2); request.setStatus(AfterSaleRequest.Status.Refunded);
        when(requests.findByIdForUpdate(3L)).thenReturn(Optional.of(request));
        assertThrows(IllegalStateException.class, () -> service.confirmReturned(3L));
        verify(orderService, never()).refundReturnedOrder(any());
    }
}
