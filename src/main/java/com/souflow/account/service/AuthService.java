package com.souflow.account.service;

import com.souflow.account.dto.AccountResponse;
import com.souflow.account.dto.AuthResponse;
import com.souflow.account.dto.ForgotPasswordRequest;
import com.souflow.account.dto.LoginRequest;
import com.souflow.account.dto.RegisterRequest;
import com.souflow.account.dto.ResetPasswordRequest;
import com.souflow.account.dto.UpdateProfileRequest;
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
            .fullName(request.getFullName())
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
        .fullName(account.getFullName())
        .email(account.getEmail())
        .phoneNumber(account.getPhoneNumber())
        .address(account.getAddress())
        .photo(account.getPhoto())
        .roleCode(account.getRole().getCode())
        .createdDate(account.getCreatedDate())
        .build();
  }

  @Transactional
  public void changePassword(
      AccountUserDetails userDetails, String currentPassword, String newPassword) {
    Account account = userDetails.getAccount();

    if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Mat khau hien tai khong dung");
    }

    account.setPassword(passwordEncoder.encode(newPassword));
    accountRepository.save(account);
  }

  @Transactional
  public void disableAccount(Long id) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tai khoan khong ton tai"));

    account.setDisabled(true);
    accountRepository.save(account);
  }

  @Transactional
  public void enableAccount(Long id) {
    Account account =
        accountRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tai khoan khong ton tai"));

    account.setDisabled(false);
    accountRepository.save(account);
  }

  @Transactional
  public void updateProfile(AccountUserDetails userDetails, UpdateProfileRequest request) {
    Account account = userDetails.getAccount();

    if (!account.getEmail().equals(request.getEmail())
        && accountRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
      throw new BusinessException(
          ErrorCode.DUPLICATE_RESOURCE, "Email da ton tai", HttpStatus.CONFLICT);
    }

    account.setFullName(request.getFullName());
    account.setEmail(request.getEmail());
    account.setPhoneNumber(request.getPhoneNumber());
    account.setAddress(request.getAddress());

    accountRepository.save(account);
  }

  @Transactional
  public void forgotPassword(ForgotPasswordRequest request) {
    Account account =
        accountRepository
            .findByEmailAndDeletedFalse(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Tai khoan khong ton tai"));
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    String email = jwtService.extractEmailFromToken(request.getToken());
    Account account =
        accountRepository
            .findByEmailAndDeletedFalse(email)
            .orElseThrow(() -> new ResourceNotFoundException("Tai khoan khong ton tai"));
    account.setPassword(passwordEncoder.encode(request.getNewPassword()));
    accountRepository.save(account);
  }
}
