package com.xiaomimall.dto;

import com.xiaomimall.entity.PaymentType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付配置数据传输对象
 * 用于支付配置的请求和响应
 */
@Data
public class PaymentConfigDTO {
    private Long id;                    // 配置ID
    private PaymentType paymentType;       // 支付类型: 1-支付宝 2-微信支付
    private String appId;              // 应用ID
    private String merchantId;         // 商户号
    private String publicKey;          // 公钥
    private String privateKey;         // 私钥
    private String notifyUrl;          // 支付通知URL
    private Boolean isActive;          // 是否启用
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
}