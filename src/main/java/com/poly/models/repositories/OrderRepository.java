package com.poly.models.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

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
	
	Optional<Order> findByCode(String code);
	
    @Query("""
        SELECT o
        FROM Order o
        LEFT JOIN o.account a
        WHERE
            (:deleted IS NULL OR o.deleted = :deleted)
            AND (:expired IS NULL OR o.expired = :expired)
            AND (
                :keyword IS NULL
                OR LOWER(o.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
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
            WHERE o.pk IN (
                SELECT o2.pk FROM Order o2
                LEFT JOIN o2.account a
                WHERE
                    (:deleted IS NULL OR o2.deleted = :deleted)
                    AND (:expired IS NULL OR o2.expired = :expired)
                    AND (
                        :keyword IS NULL
                        OR LOWER(o2.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(o2.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                    AND (:fromDate IS NULL OR o2.createdDate >= :fromDate)
                    AND (:toDate IS NULL OR o2.createdDate <= :toDate)
                    AND o2.expiredDate <= CURRENT_TIMESTAMP
            )
        """)
    int checkAndExpireBeforePagination(
    		@Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("expired") Boolean expired,
            @Param("deleted") Boolean deleted
    );
    
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.expired = true WHERE o.pk = :pk AND o.expired = false AND o.expiredDate <= CURRENT_TIMESTAMP")
    int checkAndExpire(@Param("pk") Long pk);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.deleted = true WHERE o.pk = :pk")
    int softDelete(@Param("pk") Long pk);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE orders
        SET status = 'PAID'
        WHERE pk = :orderPk
        AND (
            SELECT COALESCE(SUM(p.amount), 0)
            FROM payments p
            WHERE p.order_pk = :orderPk
            AND p.paid = 1
        ) >= total
    """, nativeQuery = true)
    int markOrderAsPaidIfFullyPaid(@Param("orderPk") Long orderPk);
}
