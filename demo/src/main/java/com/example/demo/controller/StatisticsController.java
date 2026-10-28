package com.example.demo.controller;
import com.example.demo.model.Order;
import com.example.demo.repository.ArtworksRepository;
import com.example.demo.repository.AuctionRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AccessControlService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/admin/statistics")
public class StatisticsController {
    private final AccessControlService access; private final UserRepository users; private final ArtworksRepository artworks;
    private final AuctionRepository auctions; private final OrderRepository orders;
    public StatisticsController(AccessControlService access, UserRepository users, ArtworksRepository artworks, AuctionRepository auctions, OrderRepository orders) {
        this.access=access; this.users=users; this.artworks=artworks; this.auctions=auctions; this.orders=orders;
    }
    @GetMapping public Map<String, Long> overview() {
        access.requireAdmin();
        return Map.of("users",users.count(), "artworks",artworks.count(), "auctions",auctions.count(), "orders",orders.count(),
                "paidOrders",orders.countByPaymentStatus(Order.PaymentStatus.Paid));
    }
}
