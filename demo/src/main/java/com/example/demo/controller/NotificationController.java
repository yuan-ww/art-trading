package com.example.demo.controller;
import com.example.demo.model.Notification;
import com.example.demo.service.AccessControlService;
import com.example.demo.service.NotificationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
@RestController @RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    private final AccessControlService access;
    public NotificationController(NotificationService service, AccessControlService access) { this.service = service; this.access = access; }
    @GetMapping("/users/{userId}") public List<Notification> list(@PathVariable Integer userId) { access.requireSelfOrAdmin(userId); return service.list(userId); }
    @GetMapping("/users/{userId}/page") public Page<Notification> page(@PathVariable Integer userId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) { access.requireSelfOrAdmin(userId); return service.page(userId, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by("createdAt").descending())); }
    @PutMapping("/{id}/read") public void markRead(@PathVariable Long id, @RequestParam Integer userId) { access.requireSelfOrAdmin(userId); service.markRead(id, userId); }
}
