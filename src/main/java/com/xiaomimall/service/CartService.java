package com.xiaomimall.service;

import com.xiaomimall.dto.CartItemDTO;
import com.xiaomimall.dto.CartResponseDTO;

/**
 * 购物车服务接口
 * 定义购物车相关操作
 */
public interface CartService {
    void addToCart(Long userId, CartItemDTO item);
    void updateCartItem(Long userId, CartItemDTO item);
    void removeCartItem(Long userId, Long productId);
    void clearCart(Long userId);
    CartResponseDTO getCart(Long userId);
}