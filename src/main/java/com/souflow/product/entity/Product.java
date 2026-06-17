package com.souflow.product.entity;

import com.souflow.category.entity.Category;
import com.souflow.comment.entity.Comment;
import com.souflow.common.entity.SoftDeletableEntity;
import com.souflow.discount.entity.Discount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk")
  private Long id;

  @Column(name = "id", unique = true, nullable = false, length = 50)
  private String businessId;

  @Column(name = "name_vn", nullable = false, length = 100)
  private String nameVn;

  @Column(name = "name_eng", nullable = false, length = 100)
  private String nameEng;

  @Column(name = "description_vn", nullable = false, length = 255)
  private String descriptionVn;

  @Column(name = "description_eng", nullable = false, length = 255)
  private String descriptionEng;

  @Column(name = "price", nullable = false, precision = 18, scale = 2)
  private BigDecimal price;

  @Column(name = "created_date")
  private LocalDateTime createdDate;

  @Column(name = "available", nullable = false)
  private boolean available;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "sales", nullable = false)
  private int sales;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_pk", nullable = false)
  private Category category;

  @OneToMany(mappedBy = "product")
  @Builder.Default
  private List<ProductImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "product")
  @Builder.Default
  private List<Comment> comments = new ArrayList<>();

  @ManyToMany
  @JoinTable(
      name = "products_discounts",
      joinColumns = @JoinColumn(name = "product_pk"),
      inverseJoinColumns = @JoinColumn(name = "discount_pk"))
  @Builder.Default
  private Set<Discount> discounts = new HashSet<>();
}
