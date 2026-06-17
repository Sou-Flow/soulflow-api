package com.souflow.account.entity;

import com.souflow.common.entity.SoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends SoftDeletableEntity {

  @Id
  @Column(name = "code", length = 10)
  private String code;

  @Column(name = "name_vn", nullable = false, unique = true, length = 100)
  private String nameVn;

  @Column(name = "name_eng", nullable = false, unique = true, length = 100)
  private String nameEng;
}
