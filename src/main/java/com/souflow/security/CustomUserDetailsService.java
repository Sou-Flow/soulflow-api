package com.souflow.security;

import com.souflow.account.entity.Account;
import com.souflow.account.repository.AccountRepository;
import com.souflow.common.exception.BusinessException;
import com.souflow.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final AccountRepository accountRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Account account =
        accountRepository
            .findByUsernameAndDeletedFalse(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("Tai khoan khong ton tai: " + username));

    if (account.isDisabled()) {
      throw new BusinessException(
          ErrorCode.ACCOUNT_DISABLED, "Tai khoan da bi vo hieu hoa", HttpStatus.FORBIDDEN);
    }

    return new AccountUserDetails(account);
  }
}
