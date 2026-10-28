package com.example.demo.repository;
import com.example.demo.model.ShippingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ShippingEventRepository extends JpaRepository<ShippingEvent, Long> {
    List<ShippingEvent> findByShippingIdOrderByOccurredAtAsc(Integer shippingId);
    void deleteByShippingId(Integer shippingId);
}
