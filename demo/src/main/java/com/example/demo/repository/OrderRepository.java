package com.example.demo.repository;

import com.example.demo.model.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.sql.Timestamp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.dto.OrderListItem;
import com.example.demo.dto.ParticipantOrderItem;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByBuyerId(Integer buyerId);
    List<Order> findBySellerId(Integer sellerId);
    List<Order> findByArtworkId(Integer artworkId);
    Page<Order> findByBuyerId(Integer buyerId, Pageable pageable);
    Page<Order> findBySellerId(Integer sellerId, Pageable pageable);
    @Query(value = "select new com.example.demo.dto.ParticipantOrderItem(o.orderId, o.artworkId, o.buyerId, o.sellerId, a.name, a.author, " +
            "(select min(i.imageUrl) from ArtworkImage i where i.artwork.artworkId = o.artworkId), o.finalPrice, o.createdAt, o.paymentStatus, o.shippingStatus) " +
            "from Order o left join Artwork a on a.artworkId = o.artworkId " +
            "where (:buyerId is null or o.buyerId = :buyerId) and (:sellerId is null or o.sellerId = :sellerId) " +
            "and (:keyword is null or lower(a.name) like lower(concat('%', :keyword, '%'))) " +
            "and (:paymentStatus is null or o.paymentStatus = :paymentStatus) and (:shippingStatus is null or o.shippingStatus = :shippingStatus)",
            countQuery = "select count(o) from Order o left join Artwork a on a.artworkId = o.artworkId " +
            "where (:buyerId is null or o.buyerId = :buyerId) and (:sellerId is null or o.sellerId = :sellerId) " +
            "and (:keyword is null or lower(a.name) like lower(concat('%', :keyword, '%'))) " +
            "and (:paymentStatus is null or o.paymentStatus = :paymentStatus) and (:shippingStatus is null or o.shippingStatus = :shippingStatus)")
    Page<ParticipantOrderItem> findParticipantPage(@Param("buyerId") Integer buyerId, @Param("sellerId") Integer sellerId,
                                    @Param("keyword") String keyword, @Param("paymentStatus") Order.PaymentStatus paymentStatus,
                                    @Param("shippingStatus") Order.ShippingStatus shippingStatus, Pageable pageable);
    List<Order> findByPaymentStatusAndCreatedAtLessThanEqual(Order.PaymentStatus paymentStatus, Timestamp createdAt);
    long countByPaymentStatus(Order.PaymentStatus paymentStatus);

    @Query(value = "select new com.example.demo.dto.OrderListItem(o.orderId, o.artworkId, a.name, buyer.username, seller.username, o.finalPrice, o.createdAt, o.paymentStatus, o.shippingStatus) " +
            "from Order o left join Artwork a on a.artworkId = o.artworkId left join User buyer on buyer.userId = o.buyerId left join User seller on seller.userId = o.sellerId " +
            "where (:keyword is null or lower(a.name) like lower(concat('%', :keyword, '%'))) " +
            "and (:paymentStatus is null or o.paymentStatus = :paymentStatus) and (:shippingStatus is null or o.shippingStatus = :shippingStatus)",
            countQuery = "select count(o) from Order o left join Artwork a on a.artworkId = o.artworkId " +
            "where (:keyword is null or lower(a.name) like lower(concat('%', :keyword, '%'))) " +
            "and (:paymentStatus is null or o.paymentStatus = :paymentStatus) and (:shippingStatus is null or o.shippingStatus = :shippingStatus)")
    Page<OrderListItem> findAdminPage(@Param("keyword") String keyword,
                                      @Param("paymentStatus") Order.PaymentStatus paymentStatus,
                                      @Param("shippingStatus") Order.ShippingStatus shippingStatus, Pageable pageable);

    Optional<Order> findByBuyerIdAndArtworkId(Integer userId, Integer artworkId);
    Optional<Order> findByPaymentRequestKey(String paymentRequestKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.buyerId = :buyerId and o.artworkId = :artworkId order by o.orderId desc")
    List<Order> findBuyerArtworkOrdersForUpdate(@Param("buyerId") Integer buyerId, @Param("artworkId") Integer artworkId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select orderEntity from Order orderEntity where orderEntity.orderId = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") Integer orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select orderEntity from Order orderEntity where orderEntity.buyerId = :buyerId and orderEntity.artworkId = :artworkId")
    Optional<Order> findByBuyerIdAndArtworkIdForUpdate(
            @Param("buyerId") Integer buyerId,
            @Param("artworkId") Integer artworkId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select orderEntity from Order orderEntity where orderEntity.buyerId = :buyerId and orderEntity.artworkId = :artworkId and orderEntity.paymentStatus = :paymentStatus order by orderEntity.orderId desc")
    List<Order> findByBuyerIdAndArtworkIdAndPaymentStatusForUpdate(
            @Param("buyerId") Integer buyerId,
            @Param("artworkId") Integer artworkId,
            @Param("paymentStatus") Order.PaymentStatus paymentStatus
    );
}

