package com.example.demo.repository;

import com.example.demo.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUserId(Integer id);
    User findByEmail(String email);
    User getUserByUserId(Integer id);
    List<User> findByRole(String role);
    boolean existsByEmailAndUserIdNot(String email, Integer id);
    boolean existsByUsernameAndUserIdNot(String username, Integer id);
    @Modifying
    @Transactional
    void deleteAllByUserIdIn(@Param("userIds") List<Integer> userIds);

}
