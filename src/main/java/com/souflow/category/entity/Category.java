package com.souflow.category.entity;

import com.souflow.common.entity.SoftDeletableEntity;
import com.souflow.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends SoftDeletableEntity {

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

  @Column(name = "description_vn", length = 255)
  private String descriptionVn;

  @Column(name = "description_eng", length = 255)
  private String descriptionEng;

  @OneToMany(mappedBy = "category")
  @Builder.Default
  private List<Product> products = new ArrayList<>();
}
