package com.example.demo.repository;

import com.example.demo.model.AuctionRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuctionRegistrationRepository extends JpaRepository<AuctionRegistration, Integer> {

    // 根据拍卖ID查询所有注册信息
    List<AuctionRegistration> findByAuction_AuctionId(Integer auctionId);
    long countByAuction_AuctionId(Integer auctionId);
    boolean existsByUser_UserIdAndAuction_AuctionId(Integer userId, Integer auctionId);

    // 根据用户ID查询所有注册信息
    List<AuctionRegistration> findByUser_UserId(Integer userId);


}
