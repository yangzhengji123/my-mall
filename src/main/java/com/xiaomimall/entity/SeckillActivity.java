package com.xiaomimall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动实体
 * 存储秒杀活动的配置信息
 */
@Data
public class SeckillActivity {
    private Long id;
    private Long productId;
    private BigDecimal seckillPrice;
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联的商品信息（非数据库字段）
    private Product product;
}