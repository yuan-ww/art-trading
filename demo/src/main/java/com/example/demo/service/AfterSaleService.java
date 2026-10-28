package com.example.demo.service;
import com.example.demo.model.AfterSaleRequest; import com.example.demo.model.Order; import com.example.demo.repository.AfterSaleRequestRepository; import com.example.demo.repository.OrderRepository;
import jakarta.transaction.Transactional; import org.springframework.stereotype.Service; import java.time.LocalDateTime; import java.util.Optional; import java.util.List; import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
@Service public class AfterSaleService {
 private final AfterSaleRequestRepository requests; private final OrderRepository orders; private final OrderService orderService; private final NotificationService notifications;
 public AfterSaleService(AfterSaleRequestRepository requests,OrderRepository orders,OrderService orderService,NotificationService notifications){this.requests=requests;this.orders=orders;this.orderService=orderService;this.notifications=notifications;}
 @Transactional public AfterSaleRequest request(Integer orderId,Integer buyerId,String reason){
  if(reason==null||reason.isBlank()) throw new IllegalArgumentException("请填写退货原因");
  Order order=orders.findByIdForUpdate(orderId).orElseThrow(()->new IllegalArgumentException("订单不存在"));
  if(!buyerId.equals(order.getBuyerId())||order.getPaymentStatus()!=Order.PaymentStatus.Paid||order.getShippingStatus()==Order.ShippingStatus.Pending) throw new IllegalStateException("当前订单不能申请退货");
  if(requests.findByOrderId(orderId).isPresent()) throw new IllegalStateException("该订单已有售后申请");
  AfterSaleRequest request=new AfterSaleRequest();request.setOrderId(orderId);request.setBuyerId(buyerId);request.setReason(reason.trim());
  AfterSaleRequest saved=requests.save(request); notifications.send(order.getSellerId(),"订单 " + orderId + " 有新的退货申请"); return saved;
 }
 @Transactional public Order confirmReturned(Long requestId){
  return resolveRefund(requestId, false);
 }
 @Transactional public Order resolveRefund(Long requestId, boolean administrator){
  AfterSaleRequest request=requests.findByIdForUpdate(requestId).orElseThrow(()->new IllegalArgumentException("售后申请不存在"));
  if(request.getStatus()!=AfterSaleRequest.Status.Requested && !(administrator && request.getStatus()==AfterSaleRequest.Status.Rejected)) throw new IllegalStateException("售后申请已处理");
  Order refunded=orderService.refundReturnedOrder(request.getOrderId()); request.setStatus(AfterSaleRequest.Status.Refunded);request.setProcessedAt(LocalDateTime.now());requests.save(request);return refunded;
 }
 @Transactional public AfterSaleRequest reject(Long requestId){
  AfterSaleRequest request=requests.findByIdForUpdate(requestId).orElseThrow(()->new IllegalArgumentException("售后申请不存在"));
  if(request.getStatus()!=AfterSaleRequest.Status.Requested) throw new IllegalStateException("售后申请已处理");
  request.setStatus(AfterSaleRequest.Status.Rejected);request.setProcessedAt(LocalDateTime.now());
  AfterSaleRequest result=requests.save(request); notifications.send(request.getBuyerId(),"订单 " + request.getOrderId() + " 的退货申请被拒绝，可联系管理员处理");return result;
 }
 public List<AfterSaleRequest> listAll(){return requests.findAll();}
 public Page<AfterSaleRequest> page(AfterSaleRequest.Status status, Pageable pageable){return status == null ? requests.findAll(pageable) : requests.findByStatus(status, pageable);}
 public Optional<AfterSaleRequest> byOrder(Integer orderId){return requests.findByOrderId(orderId);}
 public Optional<AfterSaleRequest> getById(Long requestId){return requests.findById(requestId);}
}
