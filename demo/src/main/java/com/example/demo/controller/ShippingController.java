package com.example.demo.controller;

import com.example.demo.model.Shipping;
import com.example.demo.service.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService shippingService;

    @Autowired
    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    // 获取订单物流信息
    @GetMapping("/getShippingByOrderId/{orderId}")
    public ResponseEntity<Shipping> getShippingByOrderId(@PathVariable int orderId) {
        Shipping shipping = shippingService.getShippingByOrderId(orderId);
        return ResponseEntity.ok(shipping);
    }
    @GetMapping("/getAllShipping")
    public ResponseEntity<Iterable<Shipping>> getAllShipping() {
        Iterable<Shipping> allShipping = shippingService.getAllShipping();
        return ResponseEntity.ok(allShipping);
    }
    // 创建物流信息
    @PostMapping("/createShipping")
    public ResponseEntity<Shipping> createShipping(@RequestBody Shipping shipping) {
        Shipping createdShipping = shippingService.createShipping(shipping);
        return ResponseEntity.status(201).body(createdShipping);
    }

    // 更新物流信息
    @PutMapping("/updateStatus")
    public ResponseEntity<String> updateShippingStatus(
            @RequestParam Integer shippingId,
            @RequestParam Shipping.ShippingStatus shippingStatus) { // 直接接收枚举
        try {
            shippingService.updateShippingStatus(shippingId, shippingStatus);
            return ResponseEntity.ok("物流状态更新成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新失败");
        }
    }
    @DeleteMapping("/deleteShipping/{shippingId}")
    public ResponseEntity<String> deleteShipping(@PathVariable Integer shippingId) {
        boolean success = shippingService.deleteShipping(shippingId);
        if (success) {
            return ResponseEntity.ok("物流信息删除成功");
        } else {
            return ResponseEntity.status(404).body("物流信息未找到");
        }
    }
}
