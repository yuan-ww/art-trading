package com.example.demo.repository;
import com.example.demo.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
 List<WalletTransaction> findByUserIdOrderByTransactionIdDesc(Integer userId);
 Page<WalletTransaction> findByUserId(Integer userId, Pageable pageable);
 Optional<WalletTransaction> findByRequestKey(String requestKey);
}
