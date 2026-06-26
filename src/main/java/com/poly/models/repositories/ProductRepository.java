package com.poly.models.repositories;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.poly.models.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	
	@Query("""
        SELECT p
        FROM Product p
        WHERE
            (:deleted IS NULL OR p.deleted = :deleted)
            AND (:available IS NULL OR p.available = :available)
            AND (:customised IS NULL OR p.customised = :customised)
            AND (:categoryPk IS NULL OR p.category.pk = :categoryPk)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (
                :keyword IS NULL
                OR LOWER(p.nameVn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.nameEng) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:fromDate IS NULL OR p.createdDate >= :fromDate)
            AND (:toDate IS NULL OR p.createdDate <= :toDate)
    """)
    Page<Product> filterProducts(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("categoryPk") Long categoryPk,
            @Param("keyword") String keyword,
            @Param("customised") Boolean customised,
            @Param("available") Boolean available,
            @Param("deleted") Boolean deleted,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
	
	@Modifying
    @Transactional
    @Query("UPDATE Product p SET p.deleted = true WHERE p.pk = :pk")
    void softDelete(@Param("pk") Long pk);

    @Modifying
	@Transactional
	@Query("UPDATE Product p SET p.quantity = :quantity WHERE p.pk = :pk")
	int updateQuantity(@Param("pk") Long pk, @Param("quantity") Integer quantity);

    @Modifying
	@Transactional
	@Query("""
		UPDATE Product p
		SET p.quantity = p.quantity - :amount,
		    p.available = CASE WHEN (p.quantity - :amount) = 0 THEN false ELSE p.available END
		WHERE p.pk = :pk
		AND p.quantity >= :amount
	""")
	int decreaseQuantity(@Param("pk") Long pk, @Param("amount") Integer amount); 
}
