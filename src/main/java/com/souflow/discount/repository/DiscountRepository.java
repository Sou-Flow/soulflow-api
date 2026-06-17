package com.souflow.discount.repository;

import com.souflow.discount.entity.Discount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

  List<Discount> findByDeletedFalseAndExpiredFalse();

  Optional<Discount> findByIdAndDeletedFalse(Long id);
}
