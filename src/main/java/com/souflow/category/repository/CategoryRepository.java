package com.souflow.category.repository;

import com.souflow.category.entity.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findByDeletedFalse();

  Optional<Category> findByIdAndDeletedFalse(Long id);

  Optional<Category> findByBusinessIdAndDeletedFalse(String businessId);

  // Sửa phần này
  boolean existsByNameVnAndDeletedFalse(String nameVn);

  boolean existsByNameVnAndDeletedFalseAndIdNot(String nameVn, Long id);
}
