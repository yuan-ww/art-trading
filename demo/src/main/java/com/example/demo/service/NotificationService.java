package com.example.demo.service;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
@Service
public class NotificationService {
    private final NotificationRepository repository;
    public NotificationService(NotificationRepository repository) { this.repository = repository; }
    public void send(Integer userId, String message) {
        Notification n = new Notification(); n.setUserId(userId); n.setMessage(message); repository.save(n);
    }
    public List<Notification> list(Integer userId) { return repository.findTop100ByUserIdOrderByCreatedAtDesc(userId); }
    public Page<Notification> page(Integer userId, Pageable pageable) { return repository.findByUserId(userId, pageable); }
    public void markRead(Long id, Integer userId) {
        Notification n = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("通知不存在"));
        if (!userId.equals(n.getUserId())) throw new IllegalArgumentException("通知不存在");
        n.setRead(true); repository.save(n);
    }
}
