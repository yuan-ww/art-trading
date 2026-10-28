package com.example.demo.controller;
import com.example.demo.model.AfterSaleRequest; import com.example.demo.model.Order; import com.example.demo.service.AccessControlService; import com.example.demo.service.AfterSaleService; import com.example.demo.service.OrderService;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import org.springframework.data.domain.Page; import org.springframework.data.domain.PageRequest; import org.springframework.data.domain.Sort;
@RestController @RequestMapping("/api/after-sales") public class AfterSaleController {
 private final AfterSaleService afterSales; private final OrderService orders; private final AccessControlService access;
 public AfterSaleController(AfterSaleService afterSales,OrderService orders,AccessControlService access){this.afterSales=afterSales;this.orders=orders;this.access=access;}
 @PostMapping("/orders/{orderId}") public ResponseEntity<?> request(@PathVariable Integer orderId,@RequestParam String reason){
  Order order=orders.getOrderById(orderId).orElseThrow(()->new IllegalArgumentException("订单不存在"));access.requireSelfOrAdmin(order.getBuyerId());
  try{return ResponseEntity.ok(afterSales.request(orderId,order.getBuyerId(),reason));}catch(IllegalArgumentException|IllegalStateException e){return ResponseEntity.badRequest().body(e.getMessage());}
 }
 @GetMapping("/orders/{orderId}") public ResponseEntity<AfterSaleRequest> byOrder(@PathVariable Integer orderId){
  Order order=orders.getOrderById(orderId).orElseThrow(()->new IllegalArgumentException("订单不存在"));
  access.requireParticipantOrAdmin(order.getBuyerId(),order.getSellerId());
  return afterSales.byOrder(orderId).map(ResponseEntity::ok).orElseGet(()->ResponseEntity.notFound().build());
 }
 @PutMapping("/{requestId}/confirm-return") public ResponseEntity<?> confirm(@PathVariable Long requestId){
  try { AfterSaleRequest request=afterSales.getById(requestId).orElseThrow(()->new IllegalArgumentException("售后申请不存在")); Order order=orders.getOrderById(request.getOrderId()).orElseThrow(()->new IllegalArgumentException("订单不存在")); access.requireSelfOrAdmin(order.getSellerId()); return ResponseEntity.ok(afterSales.confirmReturned(requestId)); } catch(IllegalArgumentException|IllegalStateException e){return ResponseEntity.badRequest().body(e.getMessage());}
 }
 @PutMapping("/{requestId}/reject") public ResponseEntity<?> reject(@PathVariable Long requestId){
  AfterSaleRequest request=afterSales.getById(requestId).orElseThrow(()->new IllegalArgumentException("售后申请不存在"));
  Order order=orders.getOrderById(request.getOrderId()).orElseThrow(()->new IllegalArgumentException("订单不存在"));
  access.requireSelfOrAdmin(order.getSellerId());
  try{return ResponseEntity.ok(afterSales.reject(requestId));}catch(IllegalArgumentException|IllegalStateException e){return ResponseEntity.badRequest().body(e.getMessage());}
 }
 @GetMapping public java.util.List<AfterSaleRequest> list(){access.requireAdmin();return afterSales.listAll();}
 @GetMapping("/page") public Page<AfterSaleRequest> page(@RequestParam(required=false) AfterSaleRequest.Status status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){access.requireAdmin();return afterSales.page(status,PageRequest.of(Math.max(0,page),Math.min(100,Math.max(1,size)),Sort.by("createdAt").descending()));}
 @PutMapping("/{requestId}/admin-refund") public ResponseEntity<?> adminRefund(@PathVariable Long requestId){
  access.requireAdmin();try{return ResponseEntity.ok(afterSales.resolveRefund(requestId,true));}catch(IllegalArgumentException|IllegalStateException e){return ResponseEntity.badRequest().body(e.getMessage());}
 }
}
