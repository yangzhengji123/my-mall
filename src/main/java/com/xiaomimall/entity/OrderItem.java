package com.xiaomimall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体
 * 代表订单中的单个商品项
 */
@Data
public class OrderItem {
    private Long id;
    private Long orderId;        // 订单ID
    private Long productId;      // 商品ID
    private String productName;  // 商品名称
    private String productImage; // 商品图片
    private BigDecimal price;    // 商品单价
    private Integer quantity;    // 购买数量
    private BigDecimal totalPrice; // 商品总价
    private LocalDateTime createdAt;
}