package com.souflow.order.entity;

import com.souflow.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders_details")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk")
  private Long id;

  @Column(name = "product_name_vn", nullable = false, length = 100)
  private String productNameVn;

  @Column(name = "product_name_eng", nullable = false, length = 100)
  private String productNameEng;

  @Column(name = "product_price", nullable = false, precision = 18, scale = 2)
  private BigDecimal productPrice;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "subtotal", nullable = false, precision = 18, scale = 2)
  private BigDecimal subtotal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_pk", nullable = false)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_pk", nullable = false)
  private Product product;
}
