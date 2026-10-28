package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.service.OrderService;
import com.example.demo.service.AccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.example.demo.repository.OrderRepository;
import com.example.demo.dto.OrderListItem;
import com.example.demo.dto.ParticipantOrderItem;
import com.example.demo.dto.ShippingContacts;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final AccessControlService accessControlService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, AccessControlService accessControlService,
                           OrderRepository orderRepository, UserRepository userRepository) {
        this.orderService = orderService;
        this.accessControlService = accessControlService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/getAllOrders")
    public List<Order> getAllOrders() {
        accessControlService.requireAdmin();
        logger.debug("Received request to fetch all orders");
        return orderService.getAllOrders();
    }

    @GetMapping("/getOrderById/{orderId}")
    public Order getOrderById(@PathVariable Integer orderId) {
        logger.debug("Received request to fetch order with ID: {}", orderId);
        Order order = orderService.getOrderById(orderId).orElseThrow(() -> {
            logger.error("Order not found with ID: {}", orderId);
            return new RuntimeException("Order not found");
        });
        accessControlService.requireParticipantOrAdmin(order.getBuyerId(), order.getSellerId());
        return order;
    }
    @GetMapping("/{orderId}/shipping-contacts")
    public ShippingContacts getShippingContacts(@PathVariable Integer orderId) {
        Order order = orderService.getOrderById(orderId).orElseThrow(() -> new IllegalArgumentException("订单不存在"));
        accessControlService.requireSelfOrAdmin(order.getSellerId());
        if (order.getPaymentStatus() != Order.PaymentStatus.Paid) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "付款后可查看收货信息");
        User seller = userRepository.findById(order.getSellerId()).orElseThrow(() -> new IllegalStateException("卖家不存在"));
        User buyer = userRepository.findById(order.getBuyerId()).orElseThrow(() -> new IllegalStateException("买家不存在"));
        return new ShippingContacts(new ShippingContacts.Contact(seller.getUsername(), seller.getAddress(), seller.getPhone()),
                new ShippingContacts.Contact(buyer.getUsername(), buyer.getAddress(), buyer.getPhone()));
    }
    @GetMapping("/getOrdersByArtworkId/{artworkId}")
    public List<Order> getOrdersByArtworkId(@PathVariable Integer artworkId) {
        logger.debug("Received request to fetch orders with auctionId: {}", artworkId);
        List<Order> orders = orderService.getOrdersByArtworkId(artworkId);
        for (Order order : orders) {
            accessControlService.requireParticipantOrAdmin(order.getBuyerId(), order.getSellerId());
        }
        return orders;
    }
    @GetMapping("/getOrdersByBuyerId/{buyerId}")
    public List<Order> getOrdersByBuyerId(@PathVariable Integer buyerId) {
        accessControlService.requireSelfOrAdmin(buyerId);
        logger.debug("Received request to fetch orders for buyerId: {}", buyerId);
        return orderService.getOrdersByBuyerId(buyerId);
    }
    @GetMapping("/getOrdersBySellerId/{sellerId}")
    public List<Order> getOrdersBySellerId(@PathVariable Integer sellerId) {
        accessControlService.requireSelfOrAdmin(sellerId);
        logger.debug("Received request to fetch orders for sellerId: {}", sellerId);
        return orderService.getOrdersBySellerId(sellerId);
    }
    @PostMapping("/createOrder")
    public Order createOrder(@RequestBody Order order) {
        accessControlService.requireSelfOrAdmin(order.getBuyerId());
        logger.debug("Received request to create new order: {}", order);
        return orderService.createDirectSaleOrder(order);
    }

    @PutMapping("/updateOrder/{orderId}")
    public ResponseEntity<String> updateOrder(@PathVariable Integer orderId, @RequestBody Order orderDetails) {
        accessControlService.requireAdmin();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("订单金额、支付和物流状态必须使用专用业务流程更新");
    }
    @GetMapping("/page")
    public Page<Order> getOrdersPage(@RequestParam(required = false) Integer buyerId,
                                     @RequestParam(required = false) Integer sellerId,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        if (buyerId != null) accessControlService.requireSelfOrAdmin(buyerId);
        else if (sellerId != null) accessControlService.requireSelfOrAdmin(sellerId);
        else accessControlService.requireAdmin();
        return orderService.getOrdersPage(buyerId, sellerId,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("orderId").descending()));
    }
    @GetMapping("/admin-page")
    public Page<OrderListItem> getAdminOrderPage(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Order.PaymentStatus paymentStatus,
                                                @RequestParam(required = false) Order.ShippingStatus shippingStatus,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        accessControlService.requireAdmin();
        String query = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return orderRepository.findAdminPage(query, paymentStatus, shippingStatus,
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("orderId").descending()));
    }
    @GetMapping("/participant-page")
    public Page<ParticipantOrderItem> getParticipantOrders(@RequestParam(required = false) Integer buyerId,
                                           @RequestParam(required = false) Integer sellerId,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Order.PaymentStatus paymentStatus,
                                           @RequestParam(required = false) Order.ShippingStatus shippingStatus,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        if ((buyerId == null) == (sellerId == null)) throw new IllegalArgumentException("必须指定一个买家或卖家");
        accessControlService.requireSelfOrAdmin(buyerId != null ? buyerId : sellerId);
        return orderRepository.findParticipantPage(buyerId, sellerId, keyword == null || keyword.isBlank() ? null : keyword.trim(),
                paymentStatus, shippingStatus, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("orderId").descending()));
    }
    @PutMapping("/updateOrderStatus")
    public ResponseEntity<?> updateOrderStatus(@RequestParam Integer userId, @RequestParam Integer artworkId, @RequestParam String status,
                                               @RequestHeader(value = "Idempotency-Key", required = false) String requestKey) {
        logger.debug("Received request to update order status for userId: {} and artworkId: {}", userId, artworkId);
        try {
            Order.PaymentStatus paymentStatus = Order.PaymentStatus.valueOf(status);
            if (paymentStatus == Order.PaymentStatus.Paid) {
                accessControlService.requireSelfOrAdmin(userId);
                return ResponseEntity.ok(orderService.payOrder(userId, artworkId, requestKey));
            }
            accessControlService.requireAdmin();
            if (paymentStatus == Order.PaymentStatus.Refunded) {
                return ResponseEntity.ok(orderService.refundOrder(userId, artworkId));
            }
            return ResponseEntity.badRequest().body("订单状态不能直接修改");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteOrder/{orderId}")
    public void deleteOrder(@PathVariable Integer orderId) {
        accessControlService.requireAdmin();
        logger.debug("Received request to delete order with ID: {}", orderId);
        orderService.deleteOrder(orderId);
    }
    @PutMapping("/handleExpiredOrder/{orderId}")
    public ResponseEntity<String> handleExpiredOrder(@PathVariable Integer orderId) {
        logger.debug("Received request to mark order {} as expired", orderId);

        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        accessControlService.requireParticipantOrAdmin(order.getBuyerId(), order.getSellerId());

        boolean updated = orderService.handleExpiredOrder(orderId);

        if (updated) {
            return ResponseEntity.ok("订单 " + orderId + " 已超时取消，艺术品重新上架");
        } else {
            return ResponseEntity.status(400).body("订单 " + orderId + " 未到支付截止时间或已处理");
        }
    }

}
