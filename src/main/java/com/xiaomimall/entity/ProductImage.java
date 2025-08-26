package com.xiaomimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品图片实体类
 * 用于存储商品的图片信息
 */
@Data
public class ProductImage {
    private Long id;            // 图片ID
    private Long productId;     // 关联Product商品ID
    private String url;         // 图片URL
    private Integer sort;       // 排序值
    private Boolean isMain;     // 是否为主图
    private LocalDateTime createdAt;
}