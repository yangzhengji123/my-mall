package com.xiaomimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 秒杀订单实体
 * 存储秒杀订单信息
 */
@Data
public class SeckillOrder {
    private Long id;//订单ID
    private Long userId;//用户ID
    private Long seckillId;//秒杀活动ID
    private String orderNo;
    private Integer status;
    private PaymentType paymentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 关联的秒杀活动信息（非数据库字段）
    private SeckillActivity seckillActivity;
}