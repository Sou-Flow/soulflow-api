package com.souflow.account.repository;

import com.souflow.account.entity.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

  Optional<Account> findByUsernameAndDeletedFalse(String username);

  Optional<Account> findByEmailAndDeletedFalse(String email);

  boolean existsByUsernameAndDeletedFalse(String username);

  boolean existsByEmailAndDeletedFalse(String email);
}
