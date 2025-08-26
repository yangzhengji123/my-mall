package com.xiaomimall.dto;

import com.xiaomimall.entity.PaymentType;
import com.xiaomimall.util.EnumValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 支付请求DTO
 * 用于发起支付请求
 */
@Data
public class PaymentRequestDTO {
    @NotNull(message = "订单号不能为空")
    private Long orderId;
    @NotNull(message = "支付方式不能为空")
    @EnumValue(enumClass = PaymentType.class, message = "无效的支付方式")
    private PaymentType paymentType;// 支付方式
    private String returnUrl; // 支付完成后的返回URL
}