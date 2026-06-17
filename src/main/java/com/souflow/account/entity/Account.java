package com.souflow.account.entity;

import com.souflow.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "pk")
  private Long id;

  @Column(name = "username", unique = true, length = 50)
  private String username;

  @Column(name = "password", length = 255)
  private String password;

  @Column(name = "fullname", nullable = false, length = 50)
  private String fullname;

  @Column(name = "email", unique = true, length = 50)
  private String email;

  @Column(name = "photo", length = 255)
  private String photo;

  @Column(name = "address", length = 100)
  private String address;

  @Column(name = "phone_number", length = 12)
  private String phoneNumber;

  @Column(name = "created_date")
  private LocalDateTime createdDate;

  @Column(name = "disabled", nullable = false)
  private boolean disabled;

  @Column(name = "credential_expired_date", nullable = false)
  private LocalDateTime credentialExpiredDate;

  @Column(name = "credential_expired", nullable = false)
  private boolean credentialExpired;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_code", referencedColumnName = "code", nullable = false)
  private Role role;
}
