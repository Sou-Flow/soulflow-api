package com.souflow.product.service;

import com.souflow.category.entity.Category;
import com.souflow.category.service.CategoryService;
import com.souflow.common.exception.ResourceNotFoundException;
import com.souflow.common.util.IdGenerator;
import com.souflow.product.dto.ProductRequest;
import com.souflow.product.dto.ProductResponse;
import com.souflow.product.entity.Product;
import com.souflow.product.repository.ProductRepository;
import java.time.LocalDateTime;
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
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryService categoryService;

  @Cacheable(value = "products", key = "'all'")
  @Transactional(readOnly = true)
  public List<ProductResponse> findAll() {
    return productRepository.findByDeletedFalse().stream().map(this::mapToResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> findByCategory(Long categoryId) {
    return productRepository.findByCategoryIdAndDeletedFalse(categoryId).stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public ProductResponse findById(Long id) {
    Product product =
        productRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("San pham khong ton tai"));
    return mapToResponse(product);
  }

  @Transactional(readOnly = true)
  public ProductResponse findByCode(String code) {
    Product product =
        productRepository
            .findByBusinessIdAndDeletedFalse(code)
            .orElseThrow(() -> new ResourceNotFoundException("San pham khong ton tai"));
    return mapToResponse(product);
  }

  @CacheEvict(value = "products", allEntries = true)
  @Transactional
  public ProductResponse create(ProductRequest request) {
    log.info("Creating product nameVn={}", request.getNameVn());
    Category category = categoryService.getEnityById(request.getCategoryId());

    Product product =
        Product.builder()
            .businessId(IdGenerator.generateBusinessId())
            .nameVn(request.getNameVn())
            .nameEng(request.getNameEng())
            .descriptionVn(request.getDescriptionVn())
            .descriptionEng(request.getDescriptionEng())
            .price(request.getPrice())
            .category(category)
            .quantity(request.getQuantity())
            .available(request.isAvailable())
            .sales(0)
            .createdDate(LocalDateTime.now())
            .build();

    return mapToResponse(productRepository.save(product));
  }

  @CacheEvict(value = "products", allEntries = true)
  @Transactional
  public ProductResponse update(Long id, ProductRequest request) {
    Product product =
        productRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("San pham khong ton tai"));

    Category category = categoryService.getEnityById(request.getCategoryId());

    product.setNameVn(request.getNameVn());
    product.setNameEng(request.getNameEng());
    product.setDescriptionVn(request.getDescriptionVn());
    product.setDescriptionEng(request.getDescriptionEng());
    product.setPrice(request.getPrice());
    product.setCategory(category);
    product.setQuantity(request.getQuantity());
    product.setAvailable(request.isAvailable());

    return mapToResponse(productRepository.save(product));
  }

  @CacheEvict(value = "products", allEntries = true)
  @Transactional
  public void delete(Long id) {
    Product product =
        productRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("San pham khong ton tai"));

    product.setDeleted(true);
    productRepository.save(product);
  }

  public Product getEntityById(Long id) {
    return productRepository
        .findByIdAndDeletedFalse(id)
        .orElseThrow(() -> new ResourceNotFoundException("San pham khong ton tai"));
  }

  public Product getEntityByIdForUpdate(Long id) {
    return productRepository
        .findByIdForUpdate(id)
        .orElseThrow(() -> new ResourceNotFoundException("San pham khong ton tai"));
  }

  private ProductResponse mapToResponse(Product product) {
    return ProductResponse.builder()
        .id(product.getId())
        .businessId(product.getBusinessId())
        .nameVn(product.getNameVn())
        .nameEng(product.getNameEng())
        .descriptionVn(product.getDescriptionVn())
        .descriptionEng(product.getDescriptionEng())
        .price(product.getPrice())
        .available(product.isAvailable())
        .quantity(product.getQuantity())
        .sales(product.getSales())
        .categoryId(product.getCategory().getId())
        .categoryNameVn(product.getCategory().getNameVn())
        .createdDate(product.getCreatedDate())
        .build();
  }
}
