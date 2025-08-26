package com.xiaomimall.controller;

import com.xiaomimall.dto.ApiResponse;
import com.xiaomimall.dto.PageResponse;
import com.xiaomimall.dto.ProductDTO;
import com.xiaomimall.dto.ProductQueryDTO;
import com.xiaomimall.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品控制器
 * 提供商品管理的API接口
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;

    // 创建商品（需要管理员权限）
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(ApiResponse.success(createdProduct));
    }

    // 更新商品（需要管理员权限）
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable Long id, 
            @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct));
    }

    // 删除商品（需要管理员权限）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable @Positive Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 获取商品详情（公开接口）
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable @Positive Long id) {
        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            // 返回200，但data为null
            return ResponseEntity.ok(ApiResponse.success(null));
            // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound("商品不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    // 商品搜索（公开接口）
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductDTO>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize) {

        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setCategoryId(categoryId);
        queryDTO.setMinPrice(minPrice);
        queryDTO.setMaxPrice(maxPrice);
        queryDTO.setStatus(status);
        queryDTO.setSortBy(sortBy);
        queryDTO.setSortOrder(sortOrder);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        PageResponse<ProductDTO> response = productService.searchProducts(queryDTO);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
