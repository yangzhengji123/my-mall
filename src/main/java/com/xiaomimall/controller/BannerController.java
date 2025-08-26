package com.xiaomimall.controller;

import com.xiaomimall.dto.ApiResponse;
import com.xiaomimall.dto.BannerDTO;
import com.xiaomimall.exception.BannerNotFoundException;
import com.xiaomimall.exception.BannerOperationException;
import com.xiaomimall.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图控制器
 * 提供首页轮播图管理的API接口
 */
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {
    
    private final ProductService productService;

    // 创建轮播图（需要管理员权限）
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerDTO>> createBanner(@RequestBody @Valid BannerDTO bannerDTO) {
        BannerDTO createdBanner = productService.createBanner(bannerDTO);
        return ResponseEntity.ok(ApiResponse.success(createdBanner));
    }

    // 更新轮播图（需要管理员权限）
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BannerDTO>> updateBanner(
            @PathVariable @Positive Long id,
            @RequestBody @Valid BannerDTO bannerDTO) {
        try {
            BannerDTO updatedBanner = productService.updateBanner(id, bannerDTO);
            return ResponseEntity.ok(ApiResponse.success(updatedBanner));
        } catch (BannerNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BannerOperationException("更新轮播图失败: " + e.getMessage());
        }
    }

    // 删除轮播图（需要管理员权限）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        productService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 获取所有启用的轮播图（公开接口）
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getActiveBanners() {
        List<BannerDTO> banners = productService.getActiveBanners();
        return ResponseEntity.ok(ApiResponse.success(banners));
    }
}
