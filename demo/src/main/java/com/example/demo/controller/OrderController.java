package com.example.demo.controller;

import com.example.demo.model.Order;
import com.example.demo.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/getAllOrders")
    public List<Order> getAllOrders() {
        logger.debug("Received request to fetch all orders");
        return orderService.getAllOrders();
    }

    @GetMapping("/getOrderById/{orderId}")
    public Order getOrderById(@PathVariable Integer orderId) {
        logger.debug("Received request to fetch order with ID: {}", orderId);
        return orderService.getOrderById(orderId).orElseThrow(() -> {
            logger.error("Order not found with ID: {}", orderId);
            return new RuntimeException("Order not found");
        });
    }
    @GetMapping("/getOrdersByArtworkId/{artworkId}")
    public List<Order> getOrdersByArtworkId(@PathVariable Integer artworkId) {
        logger.debug("Received request to fetch orders with auctionId: {}", artworkId);
        return orderService.getOrdersByArtworkId(artworkId);
    }
    @GetMapping("/getOrdersByBuyerId/{buyerId}")
    public List<Order> getOrdersByBuyerId(@PathVariable Integer buyerId) {
        logger.debug("Received request to fetch orders for buyerId: {}", buyerId);
        return orderService.getOrdersByBuyerId(buyerId);
    }
    @GetMapping("/getOrdersBySellerId/{sellerId}")
    public List<Order> getOrdersBySellerId(@PathVariable Integer sellerId) {
        logger.debug("Received request to fetch orders for sellerId: {}", sellerId);
        return orderService.getOrdersBySellerId(sellerId);
    }
    @PostMapping("/createOrder")
    public Order createOrder(@RequestBody Order order) {
        logger.debug("Received request to create new order: {}", order);
        return orderService.createOrder(order);
    }

    @PutMapping("/updateOrder/{orderId}")
    public Order updateOrder(@PathVariable Integer orderId, @RequestBody Order orderDetails) {
        logger.debug("Received request to update order with ID: {}", orderId);
        return orderService.updateOrder(orderId, orderDetails);
    }
    @PutMapping("/updateOrderStatus")
    public Order updateOrderStatus(@RequestParam Integer userId, @RequestParam Integer artworkId, @RequestParam String status) {
        logger.debug("Received request to update order status for userId: {} and artworkId: {}", userId, artworkId);
        // 先转换为枚举类型
        Order.PaymentStatus paymentStatus = Order.PaymentStatus.valueOf(status);

        // 调用 Service 层更新订单状态
        return orderService.updateOrderStatus(userId, artworkId, paymentStatus);
    }

    @DeleteMapping("/deleteOrder/{orderId}")
    public void deleteOrder(@PathVariable Integer orderId) {
        logger.debug("Received request to delete order with ID: {}", orderId);
        orderService.deleteOrder(orderId);
    }
    @PutMapping("/handleExpiredOrder/{orderId}")
    public ResponseEntity<String> handleExpiredOrder(@PathVariable Integer orderId) {
        logger.debug("Received request to mark order {} as expired", orderId);

        boolean updated = orderService.handleExpiredOrder(orderId);

        if (updated) {
            return ResponseEntity.ok("订单 " + orderId + " 处理完成：已退款，艺术品重新上架");
        } else {
            return ResponseEntity.status(400).body("订单 " + orderId + " 不存在或已处理");
        }
    }

}
