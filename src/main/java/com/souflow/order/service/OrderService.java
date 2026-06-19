package com.souflow.order.service;

import com.souflow.account.entity.Account;
import com.souflow.cart.entity.Cart;
import com.souflow.cart.entity.CartItem;
import com.souflow.cart.repository.CartRepository;
import com.souflow.common.exception.BusinessException;
import com.souflow.common.exception.ErrorCode;
import com.souflow.common.exception.ResourceNotFoundException;
import com.souflow.common.util.IdGenerator;
import com.souflow.order.dto.CreateOrderRequest;
import com.souflow.order.dto.OrderDetailResponse;
import com.souflow.order.dto.OrderResponse;
import com.souflow.order.entity.Order;
import com.souflow.order.entity.OrderDetail;
import com.souflow.order.repository.OrderRepository;
import com.souflow.product.entity.Product;
import com.souflow.product.repository.ProductRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

  private static final int ORDER_EXPIRY_HOURS = 48;
  private static final String STATUS_PENDING = "PENDING";

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final ProductRepository productRepository;

  @Transactional(readOnly = true)
  public List<OrderResponse> findByAccount(Account account) {
    return orderRepository.findOrdersByAccountId(account.getId()).stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public OrderResponse findById(Long id) {
    Order order =
        orderRepository
            .findByIdWithDetails(id)
            .orElseThrow(() -> new ResourceNotFoundException("Don hang khong ton tai"));
    return mapToResponse(order);
  }

  @Transactional
  public OrderResponse createFromCart(CreateOrderRequest request, Account account) {
    log.info(
        "Creating order from cartId={} for accountId={}", request.getCartId(), account.getId());

    Cart cart =
        cartRepository
            .findByIdWithItems(request.getCartId())
            .orElseThrow(() -> new ResourceNotFoundException("Gio hang khong ton tai"));

    if (!cart.getAccount().getId().equals(account.getId())) {
      throw new BusinessException(
          ErrorCode.FORBIDDEN, "Ban khong co quyen su dung gio hang nay", HttpStatus.FORBIDDEN);
    }

    if (cart.isExpired() || cart.getExpiredDate().isBefore(LocalDateTime.now())) {
      throw new BusinessException(ErrorCode.CART_EXPIRED, "Gio hang da het han", HttpStatus.GONE);
    }

    if (cart.getItems().isEmpty()) {
      throw new BusinessException(
          ErrorCode.BAD_REQUEST, "Gio hang trong, khong the dat hang", HttpStatus.BAD_REQUEST);
    }

    for (CartItem item : cart.getItems()) {
      Product product = item.getProduct();
      if (product.getQuantity() < item.getQuantity()) {
        throw new BusinessException(
            ErrorCode.INSUFFICIENT_STOCK,
            "San pham " + product.getNameVn() + " khong du ton kho",
            HttpStatus.BAD_REQUEST);
      }
    }

    Order order =
        Order.builder()
            .businessId(IdGenerator.generateBusinessId())
            .fullname(request.getFullname())
            .phoneNumber(request.getPhoneNumber())
            .address(request.getAddress())
            .total(cart.getTotal())
            .createdDate(LocalDateTime.now())
            .expiredDate(LocalDateTime.now().plusHours(ORDER_EXPIRY_HOURS))
            .expired(false)
            .status(STATUS_PENDING)
            .account(account)
            .orderDetails(new ArrayList<>())
            .build();

    for (CartItem cartItem : cart.getItems()) {
      Product product = cartItem.getProduct();
      product.setQuantity(product.getQuantity() - cartItem.getQuantity());
      product.setSales(product.getSales() + cartItem.getQuantity());
      productRepository.save(product);

      OrderDetail detail =
          OrderDetail.builder()
              .order(order)
              .product(product)
              .productNameVn(product.getNameVn())
              .productNameEng(product.getNameEng())
              .productPrice(product.getPrice())
              .quantity(cartItem.getQuantity())
              .subtotal(cartItem.getSubtotal())
              .build();
      order.getOrderDetails().add(detail);
    }

    cart.setDeleted(true);
    cartRepository.save(cart);

    return mapToResponse(orderRepository.save(order));
  }

  @Transactional
  public OrderResponse updateStatus(Long id, String status) {
    Order order =
        orderRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Don hang khong ton tai"));
    order.setStatus(status);
    return mapToResponse(orderRepository.save(order));
  }

  @Transactional
  public void delete(Long id) {
    Order order =
        orderRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Don hang khong ton tai"));
    order.setDeleted(true);
    orderRepository.save(order);
  }

  private OrderResponse mapToResponse(Order order) {
    List<OrderDetailResponse> details =
        order.getOrderDetails().stream()
            .map(
                detail ->
                    OrderDetailResponse.builder()
                        .id(detail.getId())
                        .productId(detail.getProduct().getId())
                        .productNameVn(detail.getProductNameVn())
                        .productNameEng(detail.getProductNameEng())
                        .productPrice(detail.getProductPrice())
                        .quantity(detail.getQuantity())
                        .subtotal(detail.getSubtotal())
                        .build())
            .toList();

    return OrderResponse.builder()
        .id(order.getId())
        .businessId(order.getBusinessId())
        .fullname(order.getFullname())
        .phoneNumber(order.getPhoneNumber())
        .address(order.getAddress())
        .total(order.getTotal())
        .status(order.getStatus())
        .expired(order.isExpired())
        .createdDate(order.getCreatedDate())
        .expiredDate(order.getExpiredDate())
        .details(details)
        .build();
  }
}
