package com.xiaomimall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * 用于表示商城中的商品信息
 */
@Data
public class Product {
    private Long id;            // 商品ID
    private String name;        // 商品名称
    private Long categoryId;    // 所属分类ID
    private BigDecimal price;   // 商品价格
    private Integer stock;      // 商品库存
    private Integer sales;      // 商品销量
    private String description; // 商品描述
    private String detail;      // 商品详情（HTML格式）
    private Integer status;     // 商品状态（0-下架，1-上架）
    private Boolean isDeleted;  // 是否删除（逻辑删除）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}