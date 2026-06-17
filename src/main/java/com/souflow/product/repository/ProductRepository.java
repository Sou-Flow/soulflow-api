package com.souflow.product.repository;

import com.souflow.product.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findByDeletedFalse();

  List<Product> findByCategoryIdAndDeletedFalse(Long categoryId);

  Optional<Product> findByIdAndDeletedFalse(Long id);

  Optional<Product> findByBusinessIdAndDeletedFalse(String businessId);
}
