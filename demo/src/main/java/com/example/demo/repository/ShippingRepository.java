package com.example.demo.repository;

import com.example.demo.model.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingRepository extends JpaRepository<Shipping, Integer> {
    // 根据订单ID查询物流信息
    Shipping findByOrderId(int orderId);
    boolean existsByOrderId(int orderId);
    Shipping findByRequestKey(String requestKey);
    Page<Shipping> findByShippingStatus(Shipping.ShippingStatus shippingStatus, Pageable pageable);
}
