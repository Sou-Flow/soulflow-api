package com.souflow.product.repository;

import com.souflow.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

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
