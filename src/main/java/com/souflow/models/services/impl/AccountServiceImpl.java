package com.souflow.models.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.souflow.models.entities.Account;
import com.souflow.models.entities.Role;
import com.souflow.models.enums.RoleCode;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.mappers.AccountMapper;
import com.souflow.models.repositories.AccountRepository;
import com.souflow.models.repositories.RoleRepository;
import com.souflow.models.requests.AccountRequest;
import com.souflow.models.requests.AuthRequest;
import com.souflow.models.requests.ForgotPasswordRequest;
import com.souflow.models.requests.ResetPasswordRequest;
import com.souflow.models.requests.VerifyOtpRequest;
import com.souflow.models.responses.AccountResponse;
import com.souflow.models.responses.AuthResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.services.AccountService;
import com.souflow.models.services.ImageService;
import com.souflow.utils.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.CacheManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
	
	private final GoogleAuthService googleAuthService;
	private final AccountRepository accountRepo;
	private final RoleRepository roleRepo;
	private final AccountMapper accountMapper;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final ImageService imageService;
	private final PasswordEncoder passwordEncoder;
	private final CacheManager cacheManager;
	private final OtpService otpService;
	private final EmailService emailService;
	
	@Override
	@Transactional
	public AuthResponse register(AccountRequest request) {
		if (accountRepo.findByUsername(request.getUsername()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
		}
		if (accountRepo.findByEmail(request.getEmail()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
		}

		Role role = roleRepo.findByCode(RoleCode.USER)
				.orElseThrow(() -> new EntityNotFoundException("Role USER not found"));

		Account account = accountMapper.toEntity(request);
		account.setPassword(passwordEncoder.encode(request.getPassword()));
		account.setRole(role);
		account.setCreatedDate(LocalDateTime.now());
		account = accountRepo.save(account);
		clearAccountCaches(account);

		String token = jwtUtil.generateToken(account.getUsername(), account.getRole().getCode().name());

		return AuthResponse.builder()
				.token(token)
				.pk(String.valueOf(account.getPk()))
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.roleCode(account.getRole().getCode().name())
				.build();
	}

	@Override
	@Transactional
	public AuthResponse login(AuthRequest authRequest) {
		// TODO Auto-generated method stub
		try {
			// Check username and password under the hood
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					authRequest.getUsername(),
					authRequest.getPassword()
				)
			);
		} catch (AuthenticationException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
		}
		
		// If spring security said ok then generate token
		Account account = accountRepo.findByUsername(authRequest.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("Username not found: " + authRequest.getUsername()));
		
		String token = jwtUtil.generateToken(account.getUsername(), account.getRole().getCode().name());
		
		// Send it back to frontend
		try {
			return AuthResponse.builder()
				.token(token)
				.pk(String.valueOf(account.getPk()))
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.roleCode(account.getRole().getCode().name())
				.url(imageService.getPublicUrl(account.getPhoto()))
				.build();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}

	}
	
	@Override
	@Transactional
	public AuthResponse loginWithGoogle(GoogleTokenDTO googleToken) {
		// TODO Auto-generated method stub 
        try {
            // 1. Verify Google token
            GoogleIdToken.Payload payload = googleAuthService.verify(googleToken.get());

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            Account account = accountRepo.findByEmail(email)
            		.orElse(null);
			if (account == null) {
				AccountRequest request = new AccountRequest();
				request.setFullname(name);
				request.setUsername(email);
				request.setEmail(email);
				Role role = roleRepo.findByCode(RoleCode.USER)
						.orElseThrow(() -> new EntityNotFoundException());
				account = accountMapper.toEntity(request);
				account.setRole(role);
				account = accountRepo.save(account);
				clearAccountCaches(account);
			}
			
            // Generate JWT
            String token = jwtUtil.generateToken(
                account.getUsername(),
                RoleCode.USER.name()
            );

            return AuthResponse.builder()
				.token(token)
				.pk(String.valueOf(account.getPk()))
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.roleCode(account.getRole().getCode().name())
				.build();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
        }
	}

	@Override
	public void forgotPassword(ForgotPasswordRequest request) {
		Account account = accountRepo.findByEmail(request.getEmail())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản với email này"));
		
		String otp = otpService.generateOtp(account.getEmail());
		try {
			emailService.sendOtpEmail(account.getEmail(), otp);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi gửi email OTP");
		}
	}

	@Override
	public void verifyOtp(VerifyOtpRequest request) {
		boolean isValid = otpService.verifyOtpWithoutDeleting(request.getEmail(), request.getOtp());
		if (!isValid) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ hoặc đã hết hạn");
		}
	}

	@Override
	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtp());
		if (!isValid) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ hoặc đã hết hạn");
		}
		
		Account account = accountRepo.findByEmail(request.getEmail())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản với email này"));
		
		account.setPassword(passwordEncoder.encode(request.getNewPassword()));
		accountRepo.save(account);
		clearAccountCaches(account);
	}
	
	@Override
	@Transactional
	public AccountResponse save(AccountRequest request) {
		Account account = accountMapper.toEntity(request);
		Account saved = accountRepo.save(account);
		clearAccountCaches(saved);
		return accountMapper.toBasicResponse(saved);
	}

	@Override
	@Transactional
	public void softDeleteByPk(Long accountPk) {
		accountRepo.softDelete(accountPk);
		Account account = accountRepo.findById(accountPk).orElse(null);
		if (account != null) {
		    clearAccountCaches(account);
		}
	}
	
	private void clearAccountCaches(Account account) {
	    if (cacheManager != null) {
	        org.springframework.cache.Cache pagesCache = cacheManager.getCache("accountPages");
	        if (pagesCache != null) pagesCache.clear();
	        
	        org.springframework.cache.Cache listCache = cacheManager.getCache("accountList");
	        if (listCache != null) {
	            if (account.getPk() != null) listCache.evict(account.getPk());
	            if (account.getUsername() != null) listCache.evict(account.getUsername());
	            if (account.getEmail() != null) listCache.evict(account.getEmail());
	        }
	        
	        org.springframework.cache.Cache detailsCache = cacheManager.getCache("accountDetailsList");
	        if (detailsCache != null) {
	            if (account.getPk() != null) detailsCache.evict(account.getPk());
	            if (account.getUsername() != null) detailsCache.evict(account.getUsername());
	            if (account.getEmail() != null) detailsCache.evict(account.getEmail());
	        }
	    }
	}

	@Override
	@Cacheable(value = "accountList", key = "#accountPk")
	public AccountResponse findByPk(Long accountPk) {
		// TODO Auto-generated method stub
		if (accountPk == null) throw new IllegalArgumentException("Can't find account when pk is null");
		Account exist = accountRepo.findById(accountPk)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with username: " + accountPk));
		return accountMapper.toBasicResponse(exist);
	}

	@Override
	@Cacheable(value = "accountList", key = "#username")
	public AccountResponse findByUsername(String username) {
		// TODO Auto-generated method stub
		Account exist = accountRepo.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with username: " + username));
		return accountMapper.toBasicResponse(exist);
	}

	@Override
	@Cacheable(value = "accountList", key = "#email")
	public AccountResponse findByEmail(String email) {
		// TODO Auto-generated method stub
		Account exist = accountRepo.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with email: " + email));
		return accountMapper.toBasicResponse(exist);
	}

	@Override
	@Cacheable(value = "accountDetailsList", key = "#accountPk")
	public AccountResponse findAccountDetailByPk(Long accountPk) {
		// TODO Auto-generated method stub
		if (accountPk == null) throw new IllegalArgumentException("Can't find account when pk is null");
		Account exist = accountRepo.findById(accountPk)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with username: " + accountPk));
		return accountMapper.toDetailResponse(exist);
	}

	@Override
	@Cacheable(value = "accountList", key = "#username")
	public AccountResponse findAccountDetailByUsername(String username) {
		// TODO Auto-generated method stub
		Account exist = accountRepo.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with username: " + username));
		return accountMapper.toDetailResponse(exist);
	}

	@Override
	@Cacheable(value = "accountList", key = "#email")
	public AccountResponse findAccountDetailByEmail(String email) {
		// TODO Auto-generated method stub
		Account exist = accountRepo.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with email: " + email));
		return accountMapper.toDetailResponse(exist);
	}

	@Override
	@Cacheable(value = "accountPages", key = "#deleted + '_' + #keyword + '_' + #fromDate + '_' + #toDate + '_' + #disabled + '_' + #role  + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
	public PageResponse<AccountResponse> filterAndPaginateAccounts(Boolean deleted, String keyword, LocalDateTime fromDate, LocalDateTime toDate, Boolean disabled, RoleCode role, SortOrder sortOrder, Integer pageNumber, Integer pageSize) {
		// TODO Auto-generated method stub
		accountRepo.checkAndExpireCredentialBeforePagination(deleted, keyword, fromDate, toDate, disabled, role);
		Sort sort = sortOrder == SortOrder.ASC
	            ? Sort.by("id").ascending()
	            : Sort.by("id").descending();
		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		Page<Account> page = accountRepo.filterAccounts(deleted, keyword, fromDate, toDate, disabled, role, pageable);
		List<AccountResponse> responses = accountMapper.toBasicResponseList(page.getContent());
		return new PageResponse<>(page, responses);
	}

	@lombok.Data
	public static class GoogleTokenDTO {
		private String token;
		public String get() { return token; }
	}
}
