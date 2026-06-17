package com.souflow.discount.entity;

import com.souflow.common.entity.SoftDeletableEntity;
import com.souflow.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discount extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk")
  private Long id;

  @Column(name = "id", unique = true, nullable = false, length = 50)
  private String businessId;

  @Column(name = "percentage")
  private Double percentage;

  @Column(name = "description_vn", length = 255)
  private String descriptionVn;

  @Column(name = "description_eng", length = 255)
  private String descriptionEng;

  @Column(name = "created_date", nullable = false)
  private LocalDateTime createdDate;

  @Column(name = "expired_date", nullable = false)
  private LocalDateTime expiredDate;

  @Column(name = "expired")
  private boolean expired;

  @ManyToMany(mappedBy = "discounts")
  @Builder.Default
  private Set<Product> products = new HashSet<>();
}
