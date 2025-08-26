package com.xiaomimall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付信息实体
 * 存储订单的支付信息
 */
@Data
public class Payment {
    private Long id;
    private Long orderId;        // 订单ID
    private String paymentNo;    // 支付平台交易号
    private PaymentType paymentType; // 支付方式
    private BigDecimal amount;   // 支付金额
    private Integer status;      // 支付状态
    private LocalDateTime payTime;    // 支付时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}