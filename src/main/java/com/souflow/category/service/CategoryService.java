package com.souflow.category.service;

import com.souflow.category.dto.CategoryRequest;
import com.souflow.category.dto.CategoryResponse;
import com.souflow.category.entity.Category;
import com.souflow.category.repository.CategoryRepository;
import com.souflow.common.exception.BusinessException;
import com.souflow.common.exception.ErrorCode;
import com.souflow.common.exception.ResourceNotFoundException;
import com.souflow.common.util.IdGenerator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Cacheable(value = "categories", key = "'all'")
  @Transactional(readOnly = true)
  public List<CategoryResponse> findAll() {
    return categoryRepository.findByDeletedFalse().stream().map(this::mapToResponse).toList();
  }

  @Transactional(readOnly = true)
  public CategoryResponse findById(Long id) {
    Category category =
        categoryRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Danh muc khong ton tai"));
    return mapToResponse(category);
  }

  @CacheEvict(value = "categories", allEntries = true)
  @Transactional
  public CategoryResponse create(CategoryRequest request) {
    log.info("Creating category nameVn={}", request.getNameVn());

    // sửa phần này
    if (categoryRepository.existsByNameVnAndDeletedFalse(request.getNameVn())) {
      throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, null);
    }
    Category category =
        Category.builder()
            .businessId(IdGenerator.generateBusinessId())
            .nameVn(request.getNameVn())
            .nameEng(request.getNameEng())
            .descriptionVn(request.getDescriptionVn())
            .descriptionEng(request.getDescriptionEng())
            .build();
    return mapToResponse(categoryRepository.save(category));
  }

  @CacheEvict(value = "categories", allEntries = true)
  @Transactional
  public CategoryResponse update(Long id, CategoryRequest request) {
    Category category =
        categoryRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Danh muc khong ton tai"));
    // Sửa phần này
    if (categoryRepository.existsByNameVnAndDeletedFalseAndIdNot(request.getNameVn(), id)) {

      throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, null);
    }

    category.setNameVn(request.getNameVn());
    category.setNameEng(request.getNameEng());
    category.setDescriptionVn(request.getDescriptionVn());
    category.setDescriptionEng(request.getDescriptionEng());

    return mapToResponse(categoryRepository.save(category));
  }

  @CacheEvict(value = "categories", allEntries = true)
  @Transactional
  public void delete(Long id) {
    Category category =
        categoryRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Danh muc khong ton tai"));
    category.setDeleted(true);
    categoryRepository.save(category);
  }

  public Category getEntityById(Long id) {
    return categoryRepository
        .findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new ResourceNotFoundException("Danh muc khong ton tai"));
  }

  private CategoryResponse mapToResponse(Category category) {
    return CategoryResponse.builder()
        .id(category.getId())
        .businessId(category.getBusinessId())
        .nameVn(category.getNameVn())
        .nameEng(category.getNameEng())
        .descriptionVn(category.getDescriptionVn())
        .descriptionEng(category.getDescriptionEng())
        .build();
  }
}
