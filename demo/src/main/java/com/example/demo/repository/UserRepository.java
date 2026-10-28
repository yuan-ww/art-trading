package com.example.demo.repository;

import com.example.demo.model.User;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUserId(Integer id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.userId = :userId")
    Optional<User> findByUserIdForUpdate(@Param("userId") Integer userId);
    User findByEmail(String email);
    User getUserByUserId(Integer id);
    List<User> findByRole(String role);
    Page<User> findByRoleAndUsernameContainingIgnoreCase(String role, String username, Pageable pageable);
    boolean existsByEmailAndUserIdNot(String email, Integer id);
    boolean existsByUsernameAndUserIdNot(String username, Integer id);
    @Modifying
    @Transactional
    void deleteAllByUserIdIn(@Param("userIds") List<Integer> userIds);

}
