package com.souflow.account.repository;

import com.souflow.account.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

  Optional<Role> findByCodeAndDeletedFalse(String code);
}
