package com.xiaomimall.controller;

import com.xiaomimall.dto.ApiResponse;
import com.xiaomimall.dto.CategoryDTO;
import com.xiaomimall.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 * 提供商品分类管理的API接口
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final ProductService productService;

    // 获取所有分类（树形结构，公开接口）
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = productService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    // 创建分类（需要管理员权限）
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@RequestBody @Valid CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = productService.createCategory(categoryDTO);
        return ResponseEntity.ok(ApiResponse.success(createdCategory));
    }

    // 更新分类（需要管理员权限）
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id, 
            @RequestBody @Valid CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = productService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedCategory));
    }

    // 删除分类（需要管理员权限）
    // 修改 deleteCategory 方法
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable @Positive Long id) {
        productService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
