package com.souflow.discount.service;

import com.souflow.common.exception.ResourceNotFoundException;
import com.souflow.common.util.IdGenerator;
import com.souflow.discount.dto.DiscountRequest;
import com.souflow.discount.dto.DiscountResponse;
import com.souflow.discount.entity.Discount;
import com.souflow.discount.repository.DiscountRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscountService {

  private final DiscountRepository discountRepository;

  @Transactional(readOnly = true)
  public List<DiscountResponse> findActive() {
    return discountRepository.findByDeletedFalseAndExpiredFalse().stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public DiscountResponse findById(Long id) {
    Discount discount =
        discountRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ma giam gia khong ton tai"));
    return mapToResponse(discount);
  }

  @Transactional
  public DiscountResponse create(DiscountRequest request) {
    log.info("Creating discount percentage={}", request.getPercentage());
    Discount discount =
        Discount.builder()
            .businessId(IdGenerator.generateBusinessId())
            .percentage(request.getPercentage())
            .descriptionVn(request.getDescriptionVn())
            .descriptionEng(request.getDescriptionEng())
            .createdDate(LocalDateTime.now())
            .expiredDate(request.getExpiredDate())
            .expired(false)
            .build();
    return mapToResponse(discountRepository.save(discount));
  }

  @Transactional
  public void delete(Long id) {
    Discount discount =
        discountRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ma giam gia khong ton tai"));
    discount.setDeleted(true);
    discountRepository.save(discount);
  }

  private DiscountResponse mapToResponse(Discount discount) {
    return DiscountResponse.builder()
        .id(discount.getId())
        .businessId(discount.getBusinessId())
        .percentage(discount.getPercentage())
        .descriptionVn(discount.getDescriptionVn())
        .descriptionEng(discount.getDescriptionEng())
        .createdDate(discount.getCreatedDate())
        .expiredDate(discount.getExpiredDate())
        .expired(discount.isExpired())
        .build();
  }
}
