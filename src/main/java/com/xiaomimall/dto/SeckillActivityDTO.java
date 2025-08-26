package com.xiaomimall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动DTO
 * 用于秒杀活动的请求和响应
 */
@Data
public class SeckillActivityDTO {
    private Long id;//秒杀活动ID
    private Long productId;//商品ID
    private BigDecimal seckillPrice;//秒杀价格
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isActive;
    private ProductDTO product; // 关联的商品信息
}