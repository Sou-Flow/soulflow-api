package com.souflow.controllers;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.DiscountRequest;
import com.souflow.models.responses.DiscountResponse;
import com.souflow.models.responses.PageResponse;
import lombok.RequiredArgsConstructor;
import com.souflow.models.services.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDiscountController {

    private final DiscountService discountService;


    @PostMapping("/discount")
    DiscountResponse save(@RequestBody DiscountRequest request) {
        return discountService.save(request);
    }

    @DeleteMapping("/discount/{pk}")
    void deleteDiscountByPk(@PathVariable Long pk) {
        discountService.softDeleteByPk(pk);
    }

    @GetMapping("/discount/{pk}")
    DiscountResponse findDiscountByPk(@PathVariable Long pk) {
        return discountService.findByPk(pk);
    }

    @GetMapping("/discount")
    PageResponse<DiscountResponse> filterAndPaginateDiscounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            @RequestParam(defaultValue = "false") Boolean expired,
            @RequestParam(defaultValue = "false") Boolean deleted,
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        
        return discountService.filterAndPaginateDiscounts(keyword, fromDate, toDate, expired, deleted, sortOrder, pageNumber, pageSize);
    }


}
