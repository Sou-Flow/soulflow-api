package com.souflow.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.CategoryRequest;
import com.souflow.models.responses.CategoryResponse;
import com.souflow.models.responses.PageResponse;
import lombok.RequiredArgsConstructor;
import com.souflow.models.services.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping("/category")
    CategoryResponse save(@RequestBody CategoryRequest request) {
        return categoryService.save(request);
    }

    @DeleteMapping("/category/{pk}")
    org.springframework.http.ResponseEntity<?> deleteCategoryByPk(@PathVariable Long pk) {
        categoryService.softDeleteByPk(pk);
        return org.springframework.http.ResponseEntity.ok().body(java.util.Map.of("message", "Deleted successfully"));
    }

    @GetMapping("/category/{pk}")
    CategoryResponse findCaregoryByPk(@PathVariable Long pk) {
        return categoryService.findByPk(pk);
    }

    @GetMapping("/category")
    PageResponse<CategoryResponse> filterAndPaginateCategories(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "false") Boolean deleted,
        @RequestParam(defaultValue = "0") Integer pageNumber,
        @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
        @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        return categoryService.filterAndPaginateCategories(keyword, deleted, sortOrder, pageNumber, pageSize);
    }

    @GetMapping("/category/list")
    List<CategoryResponse> findCategoryList() {
        return categoryService.findAll();
    }
}
