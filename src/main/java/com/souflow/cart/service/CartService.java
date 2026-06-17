package com.souflow.cart.service;

import com.souflow.account.entity.Account;
import com.souflow.cart.dto.AddCartItemRequest;
import com.souflow.cart.dto.CartItemResponse;
import com.souflow.cart.dto.CartResponse;
import com.souflow.cart.entity.Cart;
import com.souflow.cart.entity.CartItem;
import com.souflow.cart.repository.CartItemRepository;
import com.souflow.cart.repository.CartRepository;
import com.souflow.common.exception.BusinessException;
import com.souflow.common.exception.ErrorCode;
import com.souflow.common.exception.ResourceNotFoundException;
import com.souflow.common.util.IdGenerator;
import com.souflow.product.entity.Product;
import com.souflow.product.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

  private static final int CART_EXPIRY_HOURS = 24;

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductService productService;

  @Transactional(readOnly = true)
  public List<CartResponse> findByAccount(Account account) {
    return cartRepository.findActiveCartsByAccountId(account.getId()).stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public CartResponse findById(Long id) {
    Cart cart =
        cartRepository
            .findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gio hang khong ton tai"));
    validateCartNotExpired(cart);
    return mapToResponse(cart);
  }

  @Transactional
  public CartResponse createCart(Account account) {
    log.info("Creating cart for accountId={}", account.getId());
    Cart cart =
        Cart.builder()
            .businessId(IdGenerator.generateBusinessId())
            .account(account)
            .total(BigDecimal.ZERO)
            .createdDate(LocalDateTime.now())
            .expiredDate(LocalDateTime.now().plusHours(CART_EXPIRY_HOURS))
            .expired(false)
            .build();
    return mapToResponse(cartRepository.save(cart));
  }

  @Transactional
  public CartResponse addItem(Long cartId, AddCartItemRequest request) {
    Cart cart =
        cartRepository
            .findByIdWithItems(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Gio hang khong ton tai"));
    validateCartNotExpired(cart);

    Product product = productService.getEntityById(request.getProductId());
    if (!product.isAvailable() || product.getQuantity() < request.getQuantity()) {
      throw new BusinessException(
          ErrorCode.INSUFFICIENT_STOCK,
          "San pham khong du ton kho",
          org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

    CartItem existingItem =
        cartItemRepository.findByCartIdAndProductId(cartId, product.getId()).orElse(null);

    if (existingItem != null) {
      existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
      existingItem.setSubtotal(
          product.getPrice().multiply(BigDecimal.valueOf(existingItem.getQuantity())));
      cartItemRepository.save(existingItem);
    } else {
      CartItem item =
          CartItem.builder()
              .cart(cart)
              .product(product)
              .quantity(request.getQuantity())
              .subtotal(subtotal)
              .build();
      cart.getItems().add(item);
    }

    recalculateTotal(cart);
    return mapToResponse(cartRepository.save(cart));
  }

  @Transactional
  public void removeItem(Long cartId, Long itemId) {
    Cart cart =
        cartRepository
            .findByIdWithItems(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Gio hang khong ton tai"));
    validateCartNotExpired(cart);

    cart.getItems().removeIf(item -> item.getId().equals(itemId));
    recalculateTotal(cart);
    cartRepository.save(cart);
  }

  @Transactional
  public void deleteCart(Long id) {
    Cart cart =
        cartRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gio hang khong ton tai"));
    cart.setDeleted(true);
    cartRepository.save(cart);
  }

  private void recalculateTotal(Cart cart) {
    BigDecimal total =
        cart.getItems().stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    cart.setTotal(total);
  }

  private void validateCartNotExpired(Cart cart) {
    if (cart.isExpired() || cart.getExpiredDate().isBefore(LocalDateTime.now())) {
      throw new BusinessException(
          ErrorCode.CART_EXPIRED,
          "Gio hang da het han, vui long tao gio hang moi",
          org.springframework.http.HttpStatus.GONE);
    }
  }

  private CartResponse mapToResponse(Cart cart) {
    List<CartItemResponse> items =
        cart.getItems().stream()
            .map(
                item ->
                    CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productNameVn(item.getProduct().getNameVn())
                        .productNameEng(item.getProduct().getNameEng())
                        .productPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
            .toList();

    return CartResponse.builder()
        .id(cart.getId())
        .businessId(cart.getBusinessId())
        .total(cart.getTotal())
        .expired(cart.isExpired())
        .expiredDate(cart.getExpiredDate())
        .createdDate(cart.getCreatedDate())
        .items(items)
        .build();
  }
}
