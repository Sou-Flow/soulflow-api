package com.souflow.models.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.souflow.models.enums.OrderStatus;
import com.souflow.models.repositories.AccountRepository;
import com.souflow.models.repositories.OrderRepository;
import com.souflow.models.repositories.ProductRepository;
import com.souflow.models.responses.CategoryRevenueDTO;
import com.souflow.models.responses.DashboardDataDTO;
import com.souflow.models.responses.DashboardMetricsDTO;
import com.souflow.models.responses.DashboardResponse;
import com.souflow.models.responses.LowStockProductDTO;
import com.souflow.models.responses.MonthlyRevenueDTO;
import com.souflow.models.responses.TopSellingProductDTO;
import com.souflow.models.services.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;

    @Override
    public DashboardResponse getDashboardData(String filter) {
        LocalDateTime now = LocalDateTime.now();
        
        LocalDateTime currentStart;
        LocalDateTime currentEnd;
        LocalDateTime prevStart;
        LocalDateTime prevEnd;

        if ("today".equalsIgnoreCase(filter)) {
            currentStart = now.toLocalDate().atStartOfDay();
            currentEnd = now.toLocalDate().atTime(LocalTime.MAX);
            
            prevStart = currentStart.minusDays(1);
            prevEnd = currentEnd.minusDays(1);
        } else if ("week".equalsIgnoreCase(filter)) {
            // Adjust to Monday of the current week
            int currentDayOfWeek = now.getDayOfWeek().getValue();
            currentStart = now.toLocalDate().minusDays(currentDayOfWeek - 1).atStartOfDay();
            currentEnd = currentStart.toLocalDate().plusDays(6).atTime(LocalTime.MAX);
            
            prevStart = currentStart.minusWeeks(1);
            prevEnd = currentEnd.toLocalDate().minusWeeks(1).atTime(LocalTime.MAX);
        } else {
            // Default to month
            YearMonth currentYearMonth = YearMonth.of(now.getYear(), now.getMonth());
            currentStart = currentYearMonth.atDay(1).atStartOfDay();
            currentEnd = currentYearMonth.atEndOfMonth().atTime(LocalTime.MAX);
            
            YearMonth previousYearMonth = currentYearMonth.minusMonths(1);
            prevStart = previousYearMonth.atDay(1).atStartOfDay();
            prevEnd = previousYearMonth.atEndOfMonth().atTime(LocalTime.MAX);
        }

        OrderStatus status = OrderStatus.DELIVERED;

        // 1. Calculate Metrics
        BigDecimal currentTotalRevenue = orderRepository.getRevenueByMonthRange(currentStart, currentEnd, status);
        if (currentTotalRevenue == null) currentTotalRevenue = BigDecimal.ZERO;

        BigDecimal prevTotalRevenue = orderRepository.getRevenueByMonthRange(prevStart, prevEnd, status);
        if (prevTotalRevenue == null) prevTotalRevenue = BigDecimal.ZERO;

        double revenueChangePercentage = calculatePercentageChange(currentTotalRevenue, prevTotalRevenue);

        Long currentNewOrders = orderRepository.countOrdersByMonthRange(currentStart, currentEnd, status);
        if (currentNewOrders == null) currentNewOrders = 0L;

        Long prevNewOrders = orderRepository.countOrdersByMonthRange(prevStart, prevEnd, status);
        if (prevNewOrders == null) prevNewOrders = 0L;

        double ordersChangePercentage = calculatePercentageChange(BigDecimal.valueOf(currentNewOrders), BigDecimal.valueOf(prevNewOrders));

        long totalActiveUsers = accountRepository.countActiveUsers();
        long currentNewUsers = accountRepository.countUsersByMonthRange(currentStart, currentEnd);
        long prevNewUsers = accountRepository.countUsersByMonthRange(prevStart, prevEnd);
        double usersChangePercentage = calculatePercentageChange(BigDecimal.valueOf(currentNewUsers), BigDecimal.valueOf(prevNewUsers));

        long totalProducts = productRepository.countActiveProducts();
        long newProductsCount = productRepository.countProductsByMonthRange(currentStart, currentEnd);

        DashboardMetricsDTO metrics = DashboardMetricsDTO.builder()
                .totalRevenue(currentTotalRevenue)
                .revenueChangePercentage(revenueChangePercentage)
                .newOrders(currentNewOrders)
                .ordersChangePercentage(ordersChangePercentage)
                .activeUsers(totalActiveUsers)
                .usersChangePercentage(usersChangePercentage)
                .totalProducts(totalProducts)
                .newProductsCount(newProductsCount)
                .build();

        // 2. Revenue By Month (Current Year)
        int currentYear = now.getYear();
        List<Object[]> monthlyDataRaw = orderRepository.getMonthlyRevenueForYear(currentYear, status);
        Map<Integer, BigDecimal> monthDataMap = monthlyDataRaw.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO
                ));
                
        List<MonthlyRevenueDTO> revenueByMonth = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            BigDecimal rev = monthDataMap.getOrDefault(i, BigDecimal.ZERO);
            revenueByMonth.add(new MonthlyRevenueDTO("T" + i, rev));
        }

        // 3. Revenue By Category
        List<Object[]> catDataRaw = orderRepository.getRevenueByCategory(status);
        List<CategoryRevenueDTO> revenueByCategory = catDataRaw.stream().map(row -> {
            String name = (String) row[0];
            BigDecimal value = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            return new CategoryRevenueDTO(name, value);
        }).collect(Collectors.toList());

        // 4. Top Selling Products
        List<Object[]> topDataRaw = orderRepository.getTopSellingProducts(status, PageRequest.of(0, 5));
        List<TopSellingProductDTO> topSellingProducts = topDataRaw.stream().map(row -> {
            String id = (String) row[0];
            String name = (String) row[1];
            long sold = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            BigDecimal revenue = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;
            return new TopSellingProductDTO(id, name, sold, revenue);
        }).collect(Collectors.toList());

        // 5. Low Stock Products
        List<Object[]> lowStockDataRaw = productRepository.getLowStockProducts(5);
        List<LowStockProductDTO> lowStockProducts = lowStockDataRaw.stream().map(row -> {
            String id = (String) row[0];
            String name = (String) row[1];
            int stock = row[2] != null ? ((Number) row[2]).intValue() : 0;
            String prodStatus = stock == 0 ? "Hết hàng" : "Sắp hết";
            return new LowStockProductDTO(id, name, stock, prodStatus);
        }).collect(Collectors.toList());

        DashboardDataDTO dataDTO = DashboardDataDTO.builder()
                .metrics(metrics)
                .revenueByMonth(revenueByMonth)
                .revenueByCategory(revenueByCategory)
                .topSellingProducts(topSellingProducts)
                .lowStockProducts(lowStockProducts)
                .build();

        return DashboardResponse.builder()
                .code(200)
                .message("Success")
                .data(dataDTO)
                .build();
    }

    private double calculatePercentageChange(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        BigDecimal change = current.subtract(previous);
        BigDecimal percentage = change.divide(previous, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        return percentage.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
