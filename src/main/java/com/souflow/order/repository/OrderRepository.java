package com.souflow.order.repository;

import com.souflow.order.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByIdAndDeletedFalse(Long id);

  @Query(
      "SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails "
          + "WHERE o.id = :id AND o.deleted = false")
  Optional<Order> findByIdWithDetails(@Param("id") Long id);

  @Query(
      "SELECT o FROM Order o WHERE o.account.id = :accountId "
          + "AND o.deleted = false ORDER BY o.createdDate DESC")
  List<Order> findOrdersByAccountId(@Param("accountId") Long accountId);
}
