package com.souflow.cart.repository;

import com.souflow.cart.entity.Cart;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

  Optional<Cart> findByIdAndDeletedFalse(Long id);

  @Query(
      "SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product "
          + "WHERE c.id = :id AND c.deleted = false")
  Optional<Cart> findByIdWithItems(@Param("id") Long id);

  @Query(
      "SELECT c FROM Cart c WHERE c.account.id = :accountId "
          + "AND c.deleted = false AND c.expired = false")
  List<Cart> findActiveCartsByAccountId(@Param("accountId") Long accountId);

  Optional<Cart> findByBusinessIdAndDeletedFalse(String businessId);
}
