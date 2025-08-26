package com.xiaomimall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付通知DTO
 * 支付平台回调时传递的数据
 */
@Data
public class PaymentNotifyDTO {
    private String orderNo;//订单号
    private String paymentNo;//支付单号
    private BigDecimal amount;
    private LocalDateTime payTime;
    private Integer status; // 支付状态

    // 添加时间戳字段
    private Long timestamp;

    // 添加随机数字段
    private String nonce;
}