package com.xiaomimall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车响应DTO
 * 返回给前端的购物车信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDTO {
    private List<CartItemDTO> items;
    private BigDecimal totalPrice;
    private Integer totalItems;
}