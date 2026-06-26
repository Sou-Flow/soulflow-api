package com.poly.models.services.impl;

import java.math.BigDecimal;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poly.models.entities.Product;
import com.poly.models.enums.SortOrder;
import com.poly.models.mappers.ProductMapper;
import com.poly.models.repositories.ProductRepository;
import com.poly.models.requests.ProductRequest;
import com.poly.models.responses.PageResponse;
import com.poly.models.responses.ProductResponse;
import com.poly.models.services.ProductService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
	
	private final ProductRepository productRepo;
	
	private final ProductMapper productMapper;
	
	@Override
	@Transactional
	@CachePut(value = "productList", key = "#result.pk")
    @CacheEvict(value = "productPages", allEntries = true) 
	public ProductResponse save(ProductRequest request) {
	    Product product = productMapper.toEntity(request);
	    Product saved = productRepo.save(product);
	    return productMapper.toBasicResponse(saved);
	}
	
	@Override
	@Transactional
	@Caching(evict = {
	        @CacheEvict(value = "productList", key = "#productPk"),   
	        @CacheEvict(value = "productDetailList", key = "#productPk"),   
	        @CacheEvict(value = "productPages", allEntries = true) 
	    })
	public void softDeleteByPk(Long productPk) {
		if (productPk == null) throw new IllegalArgumentException("Can't find product when pk is null");
		Product exist = productRepo.findById(Long.valueOf(productPk)).
				orElseThrow(() -> new EntityNotFoundException("Product not found with Id: " + productPk));
		exist.setDeleted(true);
		productRepo.save(exist);
	}
	
	@Override
    @Cacheable(value = "productList", key = "#productPk")         
	public ProductResponse findByPk(Long productPk) {
		if (productPk == null) throw new IllegalArgumentException("Can't find product when pk is null");
		Product exist = productRepo.findById(Long.valueOf(productPk))
				.orElseThrow(() -> new UsernameNotFoundException("Product not found with Id: " + productPk));
		return productMapper.toBasicResponse(exist);
	}
	
	@Override
    @Cacheable(value = "productDetailList", key = "#productPk")         
	public ProductResponse findProductDetailByPk(Long productPk) {
		if (productPk == null) throw new IllegalArgumentException("Can't find product when pk is null");
		Product exist = productRepo.findById(Long.valueOf(productPk))
				.orElseThrow(() -> new UsernameNotFoundException("Product not found with Id: " + productPk));
		return productMapper.toDetailedResponse(exist);
	}

	@Override
    @Cacheable(value = "productPages", key = "#minPrice + '_' + #maxPrice + '_' + #categoryPk + '_' + #keyword + '_' + #customised + '_' + #available + '_' + #deleted + '_' + #fromDate + '_' + #toDate + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
	public PageResponse<ProductResponse> filterAndPaginateProducts(
			String keyword, 
			BigDecimal minPrice,
			BigDecimal maxPrice,
			Long categoryPk,
			Boolean customised,
			Boolean available,
			Boolean deleted,
			LocalDateTime fromDate,
			LocalDateTime toDate,
			SortOrder sortOrder,
			Integer pageNumber,
			Integer pageSize) {
        
		Sort sort = switch (sortOrder) {
			case DESC       -> Sort.by("id").descending();
			case ASC        -> Sort.by("id").ascending();
			case PRICE_ASC  -> Sort.by("price").ascending();
			case PRICE_DESC -> Sort.by("price").descending();
			case SALES_ASC  -> Sort.by("sales").ascending();
			case SALES_DESC -> Sort.by("sales").descending();
		};
		
		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		Page<Product> page = productRepo.filterProducts(minPrice, maxPrice, categoryPk, keyword, customised, available, deleted, fromDate, toDate, pageable);
		List<ProductResponse> responses = productMapper.toBasicResponseList(page.getContent());
        return new PageResponse<>(page, responses);

    }
	public Integer decreaseQuantity(Long pk, Integer amount) {
		return productRepo.decreaseQuantity(pk, amount);
	}
}
