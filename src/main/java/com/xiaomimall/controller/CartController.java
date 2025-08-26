package com.xiaomimall.controller;

import com.xiaomimall.dto.ApiResponse;
import com.xiaomimall.dto.CartItemDTO;
import com.xiaomimall.dto.CartResponseDTO;
import com.xiaomimall.entity.User;
import com.xiaomimall.security.CurrentUser;
import com.xiaomimall.service.CartService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;

    // 添加商品到购物车
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addToCart(
            @CurrentUser @NonNull User user,
            @RequestBody @Valid CartItemDTO item) {
        cartService.addToCart(user.getId(), item);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 更新购物车商品数量
    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateCartItem(
            @CurrentUser @NonNull User user,
            @RequestBody @Valid CartItemDTO item) {
        cartService.updateCartItem(user.getId(), item);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 从购物车移除商品
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeCartItem(
            @CurrentUser @NonNull User user,
            @PathVariable Long productId) {
        cartService.removeCartItem(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 获取购物车内容
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponseDTO>> getCart(@CurrentUser @NonNull User user) {
        CartResponseDTO cart = cartService.getCart(user.getId());
        // 处理空购物车情况
        if (cart == null) {
            // 返回空的购物车而不是null
            cart = new CartResponseDTO();
            cart.setItems(Collections.emptyList());
            cart.setTotalPrice(BigDecimal.ZERO);
            cart.setTotalItems(0);
        }
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    // 清空购物车
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @CurrentUser @NonNull User user) {
        cartService.clearCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
