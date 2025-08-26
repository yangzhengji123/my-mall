package com.xiaomimall.service;

import com.xiaomimall.dto.PaymentConfigDTO;
import com.xiaomimall.dto.PaymentNotifyDTO;
import com.xiaomimall.dto.PaymentRequestDTO;
import com.xiaomimall.dto.PaymentResponseDTO;
import com.xiaomimall.entity.PaymentType;

/**
 * 支付服务接口
 * 定义支付相关操作
 */
public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequest);
    Boolean handlePaymentNotify(PaymentNotifyDTO notifyDTO);
    Boolean queryPaymentStatus(String orderNo);
    PaymentConfigDTO getPaymentConfig(PaymentType paymentType);
    PaymentConfigDTO updatePaymentConfig(PaymentConfigDTO configDTO);
}