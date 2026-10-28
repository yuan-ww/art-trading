package com.example.demo.controller;
import com.example.demo.model.Shipping;
import com.example.demo.model.ShippingEvent;
import com.example.demo.repository.ShippingEventRepository;
import com.example.demo.service.AccessControlService;
import com.example.demo.service.ShippingService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/shipping-events")
public class ShippingEventController {
    private final ShippingEventRepository events;
    private final ShippingService shipping;
    private final AccessControlService access;
    public ShippingEventController(ShippingEventRepository events, ShippingService shipping, AccessControlService access) { this.events=events; this.shipping=shipping; this.access=access; }
    @GetMapping("/{shippingId}") public List<ShippingEvent> list(@PathVariable Integer shippingId) {
        Shipping item=shipping.getShippingById(shippingId).orElseThrow(() -> new IllegalArgumentException("物流不存在"));
        access.requireParticipantOrAdmin(item.getSenderId(), item.getReceiverId());
        return events.findByShippingIdOrderByOccurredAtAsc(shippingId);
    }
    @PostMapping("/{shippingId}") public ShippingEvent add(@PathVariable Integer shippingId, @RequestParam String description) {
        Shipping item=shipping.getShippingById(shippingId).orElseThrow(() -> new IllegalArgumentException("物流不存在"));
        access.requireSelfOrAdmin(item.getSenderId());
        if (description == null || description.isBlank() || description.length() > 500) throw new IllegalArgumentException("物流节点内容无效");
        ShippingEvent event=new ShippingEvent(); event.setShippingId(shippingId); event.setDescription(description.trim()); return events.save(event);
    }
}
