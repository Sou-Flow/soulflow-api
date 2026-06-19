package com.souflow.comment.entity;

import com.souflow.account.entity.Account;
import com.souflow.common.entity.SoftDeletableEntity;
import com.souflow.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk")
  private Long id;

  @Column(name = "content", nullable = false, length = 500)
  private String content;

  @Column(name = "created_date")
  private LocalDateTime createdDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_pk", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_pk", nullable = false)
  private Account account;

  @OneToMany(mappedBy = "comment")
  @Builder.Default
  private List<Reply> replies = new ArrayList<>();
}
