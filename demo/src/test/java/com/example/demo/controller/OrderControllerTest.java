package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AccessControlService;
import com.example.demo.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {
    @Test void participantPageRequiresOneAuthorizedOwner() {
        AccessControlService access = mock(AccessControlService.class);
        OrderRepository orders = mock(OrderRepository.class);
        OrderController controller = new OrderController(mock(OrderService.class), access, orders, mock(UserRepository.class));
        assertThrows(IllegalArgumentException.class, () -> controller.getParticipantOrders(null, null, null, null, null, 0, 20));
        assertThrows(IllegalArgumentException.class, () -> controller.getParticipantOrders(1, 2, null, null, null, 0, 20));
        when(orders.findParticipantPage(eq(1), isNull(), isNull(), isNull(), isNull(), any())).thenReturn(Page.empty());
        controller.getParticipantOrders(1, null, null, null, null, 0, 20);
        verify(access).requireSelfOrAdmin(1);
    }
    @Test void unpaidOrderNeverReturnsRecipientDetails() {
        OrderService service = mock(OrderService.class);
        AccessControlService access = mock(AccessControlService.class);
        UserRepository users = mock(UserRepository.class);
        OrderController controller = new OrderController(service, access, mock(OrderRepository.class), users);
        Order order = new Order(); order.setSellerId(4); order.setBuyerId(5); order.setPaymentStatus(Order.PaymentStatus.Pending);
        when(service.getOrderById(1)).thenReturn(Optional.of(order));
        ResponseStatusException failure = assertThrows(ResponseStatusException.class, () -> controller.getShippingContacts(1));
        assertEquals(HttpStatus.FORBIDDEN, failure.getStatusCode());
        verify(access).requireSelfOrAdmin(4);
        verifyNoInteractions(users);
    }
}
