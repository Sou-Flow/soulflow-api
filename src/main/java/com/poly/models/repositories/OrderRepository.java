package com.poly.models.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.poly.models.entities.Order;
import com.poly.models.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	
	
    @Query("""
        SELECT o
        FROM Order o
        WHERE
            (:deleted IS NULL OR o.deleted = :deleted)
            AND (:expired IS NULL OR o.expired = :expired)
            AND (
                :keyword IS NULL
                OR LOWER(o.account.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(o.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:fromDate IS NULL OR o.createdDate >= :fromDate)
            AND (:toDate IS NULL OR o.createdDate <= :toDate)
            AND (:status IS NULL OR o.status = :status)
    """)
    Page<Order> filterOrders(
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") OrderStatus status,
            @Param("expired") Boolean expired,
            @Param("deleted") Boolean deleted,
            Pageable pageable
    );
    
    @Modifying
    @Transactional
    @Query("""
            UPDATE Order o 
            SET o.expired = true
            WHERE
                (:deleted IS NULL OR o.deleted = :deleted)
                AND (:expired IS NULL OR o.expired = :expired)
                AND (
                    :keyword IS NULL
                    OR LOWER(o.account.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(o.account.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(o.account.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )
                AND (:fromDate IS NULL OR o.createdDate >= :fromDate)
                AND (:toDate IS NULL OR o.createdDate <= :toDate)
                AND o.expiredDate <= CURRENT_TIMESTAMP
        """)
    void checkAndExpireBeforePagination(
    		@Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("expired") Boolean expired,
            @Param("deleted") Boolean deleted
    );
    
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.expired = true WHERE o.pk = :pk AND o.expired = false AND o.expiredDate <= CURRENT_TIMESTAMP")
    void checkAndExpire(@Param("pk") Long pk);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.deleted = true WHERE o.pk = :pk")
    void softDelete(@Param("pk") Long pk);
}
