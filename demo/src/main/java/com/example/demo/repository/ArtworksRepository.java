package com.example.demo.repository;

import com.example.demo.model.Artwork;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.dto.ArtworkListItem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworksRepository extends JpaRepository<Artwork, Integer> {
    boolean existsByName(String name);
    @Transactional
    void deleteAllByArtworkIdIn(List<Integer> artworkIds);

    @Query("SELECT a FROM Artwork a LEFT JOIN FETCH a.images")
    List<Artwork> findAllArtworksWithImages(); // 查询所有艺术品并加载图片

    Optional<Artwork> findByArtworkId(Integer artworkId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select artwork from Artwork artwork where artwork.artworkId = :artworkId")
    Optional<Artwork> findByArtworkIdForUpdate(@Param("artworkId") Integer artworkId);

    List<Artwork> findByUserId(Integer userid);
    Page<Artwork> findByStatus(Artwork.Status status, Pageable pageable);
    Page<Artwork> findBySaleType(String saleType, Pageable pageable);
    Page<Artwork> findByStatusAndSaleType(Artwork.Status status, String saleType, Pageable pageable);

    @Query(value = "select new com.example.demo.dto.ArtworkListItem(a.artworkId, a.name, a.author, a.description, a.saleType, a.fixedPrice, cast(a.status as string), c.name, u.username, (select min(i.imageUrl) from ArtworkImage i where i.artwork.artworkId = a.artworkId)) " +
            "from Artwork a left join a.category c left join a.user u where a.status <> com.example.demo.model.Artwork.Status.审核 " +
            "and (:keyword is null or lower(a.name) like lower(concat('%', :keyword, '%')) or lower(a.author) like lower(concat('%', :keyword, '%')) or lower(u.username) like lower(concat('%', :keyword, '%'))) ",
            countQuery = "select count(a) from Artwork a left join a.user u where a.status <> com.example.demo.model.Artwork.Status.审核 " +
                    "and (:keyword is null or lower(a.name) like lower(concat('%', :keyword, '%')) or lower(a.author) like lower(concat('%', :keyword, '%')) or lower(u.username) like lower(concat('%', :keyword, '%'))) ")
    Page<ArtworkListItem> findPublicList(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "select new com.example.demo.dto.ArtworkListItem(a.artworkId, a.name, a.author, a.description, a.saleType, a.fixedPrice, cast(a.status as string), c.name, u.username, (select min(i.imageUrl) from ArtworkImage i where i.artwork.artworkId = a.artworkId)) " +
            "from Artwork a left join a.category c left join a.user u where a.status <> com.example.demo.model.Artwork.Status.审核 " +
            "and (:keyword is null or lower(a.name) like lower(concat('%', :keyword, '%')) or lower(a.author) like lower(concat('%', :keyword, '%')) or lower(u.username) like lower(concat('%', :keyword, '%')) or lower(c.name) like lower(concat('%', :keyword, '%'))) " +
            "and (:categoryFilter = false or c.name in :categories) and (:authorFilter = false or a.author in :authors) " +
            "and (:statusFilter = false or a.status in :statuses) and (:typeFilter = false or a.saleType in :types)",
            countQuery = "select count(a) from Artwork a left join a.category c left join a.user u where a.status <> com.example.demo.model.Artwork.Status.审核 " +
            "and (:keyword is null or lower(a.name) like lower(concat('%', :keyword, '%')) or lower(a.author) like lower(concat('%', :keyword, '%')) or lower(u.username) like lower(concat('%', :keyword, '%')) or lower(c.name) like lower(concat('%', :keyword, '%'))) " +
            "and (:categoryFilter = false or c.name in :categories) and (:authorFilter = false or a.author in :authors) " +
            "and (:statusFilter = false or a.status in :statuses) and (:typeFilter = false or a.saleType in :types)")
    Page<ArtworkListItem> findPublicFiltered(@Param("keyword") String keyword,
            @Param("categoryFilter") boolean categoryFilter, @Param("categories") List<String> categories,
            @Param("authorFilter") boolean authorFilter, @Param("authors") List<String> authors,
            @Param("statusFilter") boolean statusFilter, @Param("statuses") List<Artwork.Status> statuses,
            @Param("typeFilter") boolean typeFilter, @Param("types") List<String> types, Pageable pageable);
}
