package com.poly.models.services.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.poly.models.entities.Account;
import com.poly.models.entities.Role;
import com.poly.models.enums.RoleCode;
import com.poly.models.enums.SortOrder;
import com.poly.models.mappers.AccountMapper;
import com.poly.models.repositories.AccountRepository;
import com.poly.models.repositories.RoleRepository;
import com.poly.models.requests.AccountRequest;
import com.poly.models.requests.AuthRequest;
import com.poly.models.responses.AccountResponse;
import com.poly.models.responses.AuthResponse;
import com.poly.models.responses.PageResponse;
import com.poly.models.services.AccountService;
import com.poly.utils.JwtUtil;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

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
	
	@Override
	@Transactional
	public AuthResponse login(AuthRequest authRequest) {
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
		return AuthResponse.builder()
				.token(token)
				.pk(String.valueOf(account.getPk()))
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.build();
	}
	
	@Override
	@Transactional
	public AuthResponse loginWithGoogle(GoogleTokenDTO googleToken) {

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
			}
			
            // Generate JWT
            String token = jwtUtil.generateToken(
                account.getUsername(),
                RoleCode.USER.name()
            );

            return AuthResponse.builder()
				.token(token)
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.build();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
        }
	}
	
	@Override
	@Transactional
	@CachePut(value = "accountList", key = "#result.pk")
	@CacheEvict(value = "accountPages", allEntries = true)
	public AccountResponse save(AccountRequest request) {
		// TODO Auto-generated method stub
		Account account = accountMapper.toEntity(request);
		Account saved = accountRepo.save(account);
		return accountMapper.toBasicResponse(saved);
	}

	@Override
	@Transactional
	@Caching(evict = {
			@CacheEvict(value = "accountList", key = "#accountPk"), 
	        @CacheEvict(value = "accountPages", allEntries = true)
	})
	public void softDeleteByPk(Long accountPk) {
		// TODO Auto-generated method stub
		accountRepo.softDelete(accountPk);
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
