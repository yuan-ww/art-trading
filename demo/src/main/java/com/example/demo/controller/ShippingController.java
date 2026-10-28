package com.example.demo.controller;

import com.example.demo.model.Shipping;
import com.example.demo.service.ShippingService;
import com.example.demo.service.AccessControlService;
import com.example.demo.service.OrderService;
import com.example.demo.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService shippingService;
    private final AccessControlService accessControlService;
    private final OrderService orderService;

    @Autowired
    public ShippingController(
            ShippingService shippingService,
            AccessControlService accessControlService,
            OrderService orderService
    ) {
        this.shippingService = shippingService;
        this.accessControlService = accessControlService;
        this.orderService = orderService;
    }

    // 获取订单物流信息
    @GetMapping("/getShippingByOrderId/{orderId}")
    public ResponseEntity<Shipping> getShippingByOrderId(@PathVariable int orderId) {
        Shipping shipping = shippingService.getShippingByOrderId(orderId);
        if (shipping == null) {
            return ResponseEntity.notFound().build();
        }
        accessControlService.requireParticipantOrAdmin(shipping.getSenderId(), shipping.getReceiverId());
        return ResponseEntity.ok(shipping);
    }
    @GetMapping("/getAllShipping")
    public ResponseEntity<Iterable<Shipping>> getAllShipping() {
        accessControlService.requireAdmin();
        Iterable<Shipping> allShipping = shippingService.getAllShipping();
        return ResponseEntity.ok(allShipping);
    }
    @GetMapping("/page")
    public Page<Shipping> getShippingPage(@RequestParam(required = false) Shipping.ShippingStatus status,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        accessControlService.requireAdmin();
        return shippingService.getShippingPage(status, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("shippingId").descending()));
    }
    // 创建物流信息
    @PostMapping("/createShipping")
    public ResponseEntity<Shipping> createShipping(@RequestBody Shipping shipping,
                                                   @RequestHeader(value = "Idempotency-Key", required = false) String requestKey) {
        try {
            shipping.setRequestKey(requestKey);
            Order order = orderService.getOrderById(shipping.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("订单未找到"));
            accessControlService.requireSelfOrAdmin(order.getSellerId());
            Shipping createdShipping = shippingService.createShipping(shipping);
            return ResponseEntity.status(201).body(createdShipping);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 更新物流信息
    @PutMapping("/updateStatus")
    public ResponseEntity<String> updateShippingStatus(
            @RequestParam Integer shippingId,
            @RequestParam Shipping.ShippingStatus shippingStatus) { // 直接接收枚举
        Shipping shipping = shippingService.getShippingById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("物流信息未找到"));
        if (shippingStatus == Shipping.ShippingStatus.Delivered) {
            accessControlService.requireSelfOrAdmin(shipping.getReceiverId());
        } else {
            accessControlService.requireSelfOrAdmin(shipping.getSenderId());
        }
        try {
            shippingService.updateShippingStatus(shippingId, shippingStatus);
            return ResponseEntity.ok("物流状态更新成功");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/deleteShipping/{shippingId}")
    public ResponseEntity<String> deleteShipping(@PathVariable Integer shippingId) {
        Shipping shipping = shippingService.getShippingById(shippingId)
                .orElseThrow(() -> new IllegalArgumentException("物流信息未找到"));
        accessControlService.requireSelfOrAdmin(shipping.getSenderId());
        try {
            boolean success = shippingService.deleteShipping(shippingId);
            if (success) {
                return ResponseEntity.ok("物流信息删除成功，订单已恢复为待发货");
            }
            return ResponseEntity.status(404).body("物流信息未找到");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
