package com.xiaomimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付配置实体
 * 存储支付相关的配置信息
 */
@Data
public class PaymentConfig {
    private Long id;
    private PaymentType paymentType;
    private String appId;
    private String merchantId;
    private String publicKey;
    private String privateKey;
    private String notifyUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}