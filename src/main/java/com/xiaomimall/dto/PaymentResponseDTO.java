package com.xiaomimall.dto;

import com.xiaomimall.entity.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 支付响应DTO
 * 返回支付相关信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private Boolean success;
    private String message;
    private String payUrl; // 支付URL（用于重定向）
    private String qrCode; // 支付二维码（Base64编码）
    private String orderNo;
    private BigDecimal amount;
    private PaymentType paymentType;
}