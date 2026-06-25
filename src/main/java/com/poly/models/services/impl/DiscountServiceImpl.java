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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poly.models.entities.Discount;
import com.poly.models.enums.SortOrder;
import com.poly.models.mappers.DiscountMapper;
import com.poly.models.repositories.DiscountRepository;
import com.poly.models.requests.DiscountRequest;
import com.poly.models.responses.DiscountResponse;
import com.poly.models.responses.PageResponse;
import com.poly.models.services.DiscountService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepo;
    private final DiscountMapper discountMapper;

    @Override
    @Transactional
    @CachePut(value = "discountList", key = "#result.pk")
    @CacheEvict(value = "discountPages", allEntries = true)
    public DiscountResponse save(DiscountRequest request) {
        Discount discount = discountMapper.toEntity(request);
        Discount saved = discountRepo.save(discount);
        return discountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "discountList", key = "#discountPk"),
        @CacheEvict(value = "discountPages", allEntries = true)
    })
    public void softDeleteByPk(Long discountPk) {
        discountRepo.softDelete(discountPk);
    }

    @Override
    @Cacheable(value = "discountList", key = "#discountPk")
    public DiscountResponse findByPk(Long discountPk) {
        if (discountPk == null) throw new IllegalArgumentException("Can't not find discount when pk is null");
        Discount discount = discountRepo.findById(discountPk)
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with pk: " + discountPk));
        return discountMapper.toResponse(discount);
    }

    @Override
    @Cacheable(value = "discountPages", key = "#keyword + '_' + #fromDate + '_' + #toDate + '_' + #expired + '_' + #deleted + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
    public PageResponse<DiscountResponse> filterAndPaginateDiscounts(String keyword, LocalDateTime fromDate, LocalDateTime toDate,
            Boolean expired, Boolean deleted, SortOrder sortOrder, Integer pageNumber, Integer pageSize) {
    	discountRepo.checkAndExpireBeforePagination(keyword, fromDate, toDate, expired, deleted);
        Sort sort = sortOrder == SortOrder.ASC
                ? Sort.by("id").ascending()
                : Sort.by("id").descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Discount> page = discountRepo.filterDiscounts(keyword, fromDate, toDate, expired, deleted, pageable);
        List<DiscountResponse> responses = discountMapper.toResponseList(page.getContent());
        return new PageResponse<>(page, responses);
    }
}
