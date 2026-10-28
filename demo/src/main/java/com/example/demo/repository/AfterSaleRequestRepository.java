package com.example.demo.repository;
import com.example.demo.model.AfterSaleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface AfterSaleRequestRepository extends JpaRepository<AfterSaleRequest, Long> {
 Optional<AfterSaleRequest> findByOrderId(Integer orderId);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select r from AfterSaleRequest r where r.requestId = :id")
 Optional<AfterSaleRequest> findByIdForUpdate(@Param("id") Long id);
 Page<AfterSaleRequest> findByStatus(AfterSaleRequest.Status status, Pageable pageable);
}
