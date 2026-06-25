package com.souflow.product.repository;

import com.souflow.product.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deleted = false")
  Optional<Product> findByIdForUpdate(@Param("id") Long id);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.deleted = false")
  List<Product> findByDeletedFalse();

  @Query(
      "SELECT p FROM Product p LEFT JOIN FETCH p.category"
          + " WHERE p.category.id = :categoryId AND p.deleted = false")
  List<Product> findByCategoryIdAndDeletedFalse(Long categoryId);

  @Query(
      "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.id = :id AND p.deleted = false")
  Optional<Product> findByIdAndDeletedFalse(Long id);

  Optional<Product> findByBusinessIdAndDeletedFalse(String businessId);
}
