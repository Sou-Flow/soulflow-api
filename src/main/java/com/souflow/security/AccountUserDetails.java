package com.souflow.security;

import com.souflow.account.entity.Account;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class AccountUserDetails implements UserDetails {

  private final Account account;

  public AccountUserDetails(Account account) {
    this.account = account;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().getCode()));
  }

  @Override
  public String getPassword() {
    return account.getPassword();
  }

  @Override
  public String getUsername() {
    return account.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return !account.isCredentialExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return !account.isDisabled();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return !account.isCredentialExpired();
  }

  @Override
  public boolean isEnabled() {
    return !account.isDisabled() && !account.isDeleted();
  }
}
