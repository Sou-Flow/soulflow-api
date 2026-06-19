package com.souflow.order.entity;

import com.souflow.account.entity.Account;
import com.souflow.common.entity.SoftDeletableEntity;
import com.souflow.payment.entity.Payment;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk")
  private Long id;

  @Column(name = "id", unique = true, nullable = false, length = 50)
  private String businessId;

  @Column(name = "fullname", nullable = false, length = 100)
  private String fullname;

  @Column(name = "phone_number", nullable = false, length = 12)
  private String phoneNumber;

  @Column(name = "address", nullable = false, length = 100)
  private String address;

  @Column(name = "total", nullable = false, precision = 18, scale = 2)
  private BigDecimal total;

  @Column(name = "created_date")
  private LocalDateTime createdDate;

  @Column(name = "expired_date", nullable = false)
  private LocalDateTime expiredDate;

  @Column(name = "expired", nullable = false)
  private boolean expired;

  @Column(name = "status", nullable = false, length = 50)
  private String status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_pk", nullable = false)
  private Account account;

  @OneToMany(mappedBy = "order")
  @Builder.Default
  private List<OrderDetail> orderDetails = new ArrayList<>();

  @OneToMany(mappedBy = "order")
  @Builder.Default
  private List<Payment> payments = new ArrayList<>();
}
