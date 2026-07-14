package com.souflow.models.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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

import com.souflow.models.entities.Order;
import com.souflow.models.enums.OrderStatus;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.mappers.OrderMapper;
import com.souflow.models.repositories.OrderRepository;
import com.souflow.models.repositories.ProductRepository;
import com.souflow.models.requests.OrderRequest;
import com.souflow.models.responses.OrderResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.services.OrderService;

import com.souflow.models.responses.NotificationMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

	private final OrderMapper orderMapper;
	
    private final OrderRepository orderRepo;

    private final ProductRepository productRepo;

    private final CacheManager cacheManager;

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    @CachePut(value = "orderList", key = "T(Long).valueOf(#result.pk)")
    @Caching(evict = {
    	@CacheEvict(value = "orderPages", allEntries = true)
    })
    public OrderResponse save(OrderRequest request) {
        Order order = orderMapper.toEntity(request);
        
        if (request.getPk() == null && order.getOrderDetails() != null) {
            order.getOrderDetails().forEach(detail -> {
                int updatedRows = productRepo.decreaseQuantity(detail.getProduct().getPk(), detail.getQuantity());
                if (updatedRows == 0) {
                    throw new IllegalArgumentException("Sản phẩm không đủ số lượng tồn kho (hoặc không tồn tại).");
                }
                
                int oldQuantity = detail.getProduct().getQuantity();
                int newQuantity = oldQuantity - detail.getQuantity();
                if (oldQuantity > 10 && newQuantity <= 10) {
                    NotificationMessage msg = NotificationMessage.builder()
                        .type("LOW_STOCK")
                        .title("Cảnh báo kho")
                        .message("Sản phẩm " + detail.getProduct().getNameVn() + " sắp hết hàng (" + newQuantity + " sản phẩm).")
                        .referenceId(detail.getProduct().getCode())
                        .timestamp(LocalDateTime.now().toString())
                        .build();
                    messagingTemplate.convertAndSend("/topic/admin.notifications", msg);
                }
            });
        }
        
        Order saved = orderRepo.save(order);
        clearRelatedCaches(saved.getPk());
        return orderMapper.toResponse(saved);
    }
    
    @Override
    @Transactional
    @Caching(evict = {
    	@CacheEvict(value = "orderList", key = "#orderPk"),
    	@CacheEvict(value = "orderPages", allEntries = true)
    })
    public void softDeleteByPk(Long orderPk) {
        Order exist = orderRepo.findById(orderPk)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with Pk: " + orderPk));
        
        if (exist.getDeleted() == null || !exist.getDeleted()) {
            if (exist.getOrderDetails() != null) {
                exist.getOrderDetails().forEach(detail -> {
                    productRepo.increaseQuantity(detail.getProduct().getPk(), detail.getQuantity());
                });
            }
            orderRepo.softDelete(orderPk);
            clearRelatedCaches(orderPk);
        }
    }

    @Override
    @Cacheable(value = "orderList", key = "#orderPk")
    public OrderResponse findByPk(Long orderPk) {
        if (orderPk == null) throw new IllegalArgumentException("Can't not find order when pk is null");
        Order exist = orderRepo.findById(orderPk)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with Pk: " + orderPk));
        // Force load lazy orderDetails để tránh trường hợp bị null khi serialize vào cache
        if (exist.getOrderDetails() != null) {
            exist.getOrderDetails().size();
        }
        return orderMapper.toResponse(exist);
    }

    @Override
    @Cacheable(value = "orderPages", key = "#keyword + '_' + #accountPk + '_' + #fromDate + '_' + #toDate + '_' + #status + '_' + #expired + '_' + #deleted + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
    public PageResponse<OrderResponse> filterAndPaginateOrders(
            String keyword,
            Long accountPk,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            OrderStatus status,
            Boolean expired,
            Boolean deleted,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize) {
    	Sort sort = sortOrder == SortOrder.ASC
	            ? Sort.by("pk").ascending()
	            : Sort.by("pk").descending();
    	Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
    	Page<Order> page = orderRepo.filterOrders(keyword, accountPk, fromDate, toDate, status, expired, deleted, pageable);
    	List<OrderResponse> responses = orderMapper.toResponseList(page.getContent());
        return new PageResponse<>(page, responses);
    }

    @Override
    @Cacheable(value = "orderPages", key = "'active_' + #keyword + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
    public PageResponse<OrderResponse> filterAndPaginateActiveOrders(
            String keyword,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize
    ) {
        checkAndExpireBeforePagination(keyword, null, null, null, null, false);
        
        Sort sort = sortOrder == SortOrder.ASC
                ? Sort.by("createdDate").ascending()
                : Sort.by("createdDate").descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Order> page = orderRepo.filterActiveOrders(
                keyword,
                pageable
        );

        List<OrderResponse> responses = orderMapper.toResponseList(page.getContent());
        return new PageResponse<>(page, responses);
    }

    @Override
    public void checkAndExpireBeforePagination(
            String keyword,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            OrderStatus status,
            Boolean expired,
            Boolean deleted
    ) {
        int effectedRows = orderRepo.checkAndExpireBeforePagination(keyword, fromDate, toDate, expired, deleted);
        if (effectedRows != 0) {
            Cache cache = cacheManager.getCache("orderPages");
            cache.clear();
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
    	@CacheEvict(value = "orderList", key = "#orderPk"),
    	@CacheEvict(value = "orderPages", allEntries = true)
    })
    public Integer markOrderAsPaidIfFullyPaid(Long orderPk) {
        Integer effectedRows = orderRepo.markOrderAsPaidIfFullyPaid(orderPk);
        if (effectedRows != null && effectedRows > 0) {
            Order order = orderRepo.findById(orderPk).orElse(null);
            if (order != null && order.getOrderDetails() != null) {
                order.getOrderDetails().forEach(detail -> {
                    productRepo.increaseSales(detail.getProduct().getPk(), detail.getQuantity());
                });
                
                NotificationMessage msg = NotificationMessage.builder()
                    .type("ORDER_PAID")
                    .title("Đơn hàng đã thanh toán")
                    .message("Đơn hàng " + order.getCode() + " vừa được thanh toán thành công.")
                    .referenceId(order.getCode())
                    .timestamp(LocalDateTime.now().toString())
                    .build();
                messagingTemplate.convertAndSend("/topic/admin.notifications", msg);
            }
            clearRelatedCaches(orderPk);
        }
        return effectedRows;
    }

    @Override
    @Transactional
    @Caching(evict = {
    	@CacheEvict(value = "orderList", key = "#orderPk"),
    	@CacheEvict(value = "orderPages", allEntries = true)
    })
    public void markOrderAsPaidUnconditionally(Long orderPk) {
        Order order = orderRepo.findById(orderPk).orElse(null);
        if (order != null && order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            orderRepo.save(order);
            if (order.getOrderDetails() != null) {
                order.getOrderDetails().forEach(detail -> {
                    productRepo.increaseSales(detail.getProduct().getPk(), detail.getQuantity());
                });
            }
            
            NotificationMessage msg = NotificationMessage.builder()
                .type("ORDER_PAID")
                .title("Đơn hàng đã thanh toán")
                .message("Đơn hàng " + order.getCode() + " vừa được thanh toán thành công.")
                .referenceId(order.getCode())
                .timestamp(LocalDateTime.now().toString())
                .build();
            messagingTemplate.convertAndSend("/topic/admin.notifications", msg);
            clearRelatedCaches(orderPk);
        }
    }

    @Override
    @Transactional
    public void increaseSalesForOrder(Long orderPk) {
        Order order = orderRepo.findById(orderPk).orElse(null);
        if (order != null && order.getOrderDetails() != null) {
            order.getOrderDetails().forEach(detail -> {
                productRepo.increaseSales(detail.getProduct().getPk(), detail.getQuantity());
            });
            
            String title = "STORE".equalsIgnoreCase(order.getPaymentMethod()) ? "Đơn hàng mới (Tại cửa hàng)" : "Đơn hàng mới (COD)";
            String methodText = "STORE".equalsIgnoreCase(order.getPaymentMethod()) ? "nhận tại cửa hàng" : "thanh toán khi nhận hàng";
            
            NotificationMessage msg = NotificationMessage.builder()
                .type("NEW_ORDER")
                .title(title)
                .message("Đơn hàng " + order.getCode() + " vừa được đặt (" + methodText + ").")
                .referenceId(order.getCode())
                .timestamp(LocalDateTime.now().toString())
                .build();
            messagingTemplate.convertAndSend("/topic/admin.notifications", msg);
            clearRelatedCaches(orderPk);
        }
    }

    @Override
    @Transactional
    @Caching(evict = {
    	@CacheEvict(value = "orderList", key = "#orderPk"),
    	@CacheEvict(value = "orderPages", allEntries = true),
        @CacheEvict(value = "productPages", allEntries = true)
    })
    public void updateStatus(Long orderPk, OrderStatus status) {
        Order order = orderRepo.findById(orderPk)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with Pk: " + orderPk));
        
        if (status == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            if (order.getOrderDetails() != null) {
                order.getOrderDetails().forEach(detail -> {
                    productRepo.increaseQuantity(detail.getProduct().getPk(), detail.getQuantity());
                });
            }
            orderRepo.save(order);
            clearRelatedCaches(orderPk);
        } else if (order.getStatus() != status) {
            order.setStatus(status);
            orderRepo.save(order);
            // If marked as PAID manually, SePay logic also does it unconditionally but this is a fallback
            if (status == OrderStatus.PAID && order.getOrderDetails() != null) {
                order.getOrderDetails().forEach(detail -> {
                    productRepo.increaseSales(detail.getProduct().getPk(), detail.getQuantity());
                });
                
                NotificationMessage msg = NotificationMessage.builder()
                    .type("ORDER_PAID")
                    .title("Đơn hàng đã thanh toán")
                    .message("Đơn hàng " + order.getCode() + " vừa được thanh toán thành công.")
                    .referenceId(order.getCode())
                    .timestamp(LocalDateTime.now().toString())
                    .build();
                messagingTemplate.convertAndSend("/topic/admin.notifications", msg);
            }

            String statusVn = switch(status) {
                case PENDING -> "Chờ xử lý";
                case WAITING_PAYMENT -> "Chờ thanh toán";
                case PAID -> "Đã thanh toán";
                case PROCESSING -> "Đang xử lý";
                case SHIPPED -> "Đang giao hàng";
                case DELIVERED -> "Đã giao hàng";
                case CANCELLED -> "Đã hủy";
            };

            // Realtime update cho User khi trạng thái thay đổi
            NotificationMessage userUpdateMsg = NotificationMessage.builder()
                .type("ORDER_STATUS_CHANGED")
                .title("Cập nhật trạng thái đơn hàng")
                .message("Đơn hàng " + order.getCode() + " đã chuyển sang trạng thái " + statusVn)
                .referenceId(String.valueOf(order.getPk()))
                .status(status.name())
                .timestamp(LocalDateTime.now().toString())
                .build();
            // Gửi qua kênh chung của user có kèm username để FE dễ filter, hoặc gửi kênh riêng của order đó
            if (order.getAccount() != null && order.getAccount().getUsername() != null) {
                messagingTemplate.convertAndSend("/topic/user.notifications." + order.getAccount().getUsername(), userUpdateMsg);
            }
            // Gửi thêm vào kênh riêng của đơn hàng (dùng khi khách đang xem chi tiết hoặc lịch sử đơn hàng đó)
            messagingTemplate.convertAndSend("/topic/order." + order.getCode(), userUpdateMsg);

            clearRelatedCaches(orderPk);
        }
    }

    private void clearRelatedCaches(Long orderPk) {
        if (cacheManager != null) {
            org.springframework.cache.Cache orderPagesCache = cacheManager.getCache("orderPages");
            if (orderPagesCache != null) orderPagesCache.clear();
            org.springframework.cache.Cache orderListCache = cacheManager.getCache("orderList");
            if (orderListCache != null) orderListCache.evict(orderPk);
            
            Order order = orderRepo.findById(orderPk).orElse(null);
            if (order != null && order.getOrderDetails() != null) {
                order.getOrderDetails().forEach(detail -> {
                    Long productPk = detail.getProduct().getPk();
                    org.springframework.cache.Cache productPagesCache = cacheManager.getCache("productPages");
                    if (productPagesCache != null) productPagesCache.clear();
                    org.springframework.cache.Cache productListCache = cacheManager.getCache("productList");
                    if (productListCache != null) productListCache.evict(productPk);
                    org.springframework.cache.Cache productDetailListCache = cacheManager.getCache("productDetailList");
                    if (productDetailListCache != null) productDetailListCache.evict(productPk);
                });
            }
        }
    }
}
