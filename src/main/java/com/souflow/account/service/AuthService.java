package com.souflow.account.service;

import com.souflow.account.dto.AccountResponse;
import com.souflow.account.dto.AuthResponse;
import com.souflow.account.dto.LoginRequest;
import com.souflow.account.dto.RegisterRequest;
import com.souflow.account.entity.Account;
import com.souflow.account.entity.Role;
import com.souflow.account.repository.AccountRepository;
import com.souflow.account.repository.RoleRepository;
import com.souflow.common.exception.BusinessException;
import com.souflow.common.exception.ErrorCode;
import com.souflow.common.exception.ResourceNotFoundException;
import com.souflow.security.AccountUserDetails;
import com.souflow.security.JwtService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

  private static final String DEFAULT_ROLE = "CUSTOMER";

  private final AccountRepository accountRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    log.info("Registering account with username={}", request.getUsername());

    if (accountRepository.existsByUsernameAndDeletedFalse(request.getUsername())) {
      throw new BusinessException(
          ErrorCode.DUPLICATE_RESOURCE, "Username da ton tai", HttpStatus.CONFLICT);
    }
    if (accountRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
      throw new BusinessException(
          ErrorCode.DUPLICATE_RESOURCE, "Email da ton tai", HttpStatus.CONFLICT);
    }

    Role role =
        roleRepository
            .findByCodeAndDeletedFalse(DEFAULT_ROLE)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Vai tro mac dinh CUSTOMER chua duoc cau hinh trong he thong"));

    Account account =
        Account.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .fullname(request.getFullname())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .address(request.getAddress())
            .createdDate(LocalDateTime.now())
            .disabled(false)
            .credentialExpired(false)
            .credentialExpiredDate(LocalDateTime.now().plusYears(1))
            .role(role)
            .build();

    accountRepository.save(account);

    AccountUserDetails userDetails = new AccountUserDetails(account);
    String token = jwtService.generateToken(userDetails);

    return AuthResponse.builder()
        .accessToken(token)
        .tokenType("Bearer")
        .username(account.getUsername())
        .role(role.getCode())
        .build();
  }

  public AuthResponse login(LoginRequest request) {
    log.info("Login attempt for username={}", request.getUsername());

    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    AccountUserDetails userDetails = (AccountUserDetails) authentication.getPrincipal();
    String token = jwtService.generateToken(userDetails);

    return AuthResponse.builder()
        .accessToken(token)
        .tokenType("Bearer")
        .username(userDetails.getUsername())
        .role(userDetails.getAccount().getRole().getCode())
        .build();
  }

  @Transactional(readOnly = true)
  public AccountResponse getCurrentAccount(AccountUserDetails userDetails) {
    Account account = userDetails.getAccount();
    return mapToResponse(account);
  }

  private AccountResponse mapToResponse(Account account) {
    return AccountResponse.builder()
        .id(account.getId())
        .username(account.getUsername())
        .fullname(account.getFullname())
        .email(account.getEmail())
        .phoneNumber(account.getPhoneNumber())
        .address(account.getAddress())
        .photo(account.getPhoto())
        .roleCode(account.getRole().getCode())
        .createdDate(account.getCreatedDate())
        .build();
  }
}
