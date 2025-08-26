package com.xiaomimall.service;

import com.xiaomimall.dto.PaymentConfigDTO;
import com.xiaomimall.dto.PaymentNotifyDTO;
import com.xiaomimall.dto.PaymentRequestDTO;
import com.xiaomimall.dto.PaymentResponseDTO;
import com.xiaomimall.entity.*;
import com.xiaomimall.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付服务实现（支付宝集成示例）
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentConfigMapper paymentConfigMapper;
    private final OrderMapper orderMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final PaymentMapper paymentMapper;
    private final SeckillActivityMapper seckillActivityMapper;

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequest) {
        // 1. 验证订单是否存在
        Order order = orderMapper.findById(paymentRequest.getOrderId());
        if (order == null) {
            // 检查是否是秒杀订单
            SeckillOrder seckillOrder = seckillOrderMapper.findByOrderNo(String.valueOf(paymentRequest.getOrderId()));
            if (seckillOrder == null) {
                return new PaymentResponseDTO(
                        false,
                        "订单不存在",
                        null,
                        null,
                        null,
                        null,
                        paymentRequest.getPaymentType()
                );
            }

            // 秒杀订单支付处理
            return createSeckillPayment(seckillOrder, paymentRequest);
        }

        // 2. 验证订单状态
        if (order.getStatus() != OrderStatus.PENDING.getCode()) {
            return new PaymentResponseDTO(
                    false,
                    "订单状态异常",
                    null,
                    null,
                    order.getOrderNo(),
                    order.getPaymentAmount(),
                    order.getPaymentType()
            );
        }

        // 3. 根据支付类型获取配置
        PaymentType paymentType = paymentRequest.getPaymentType();
        PaymentConfig config = paymentConfigMapper.findByPaymentType(paymentType);
        if (config == null) {
            return new PaymentResponseDTO(
                    false,
                    "支付方式暂不支持",
                    null,
                    null,
                    order.getOrderNo(),
                    order.getPaymentAmount(),
                    order.getPaymentType()
            );
        }

        // 4. 调用支付平台API创建支付
        try {
            // 这里以支付宝为例，实际项目中需要根据支付平台API实现
            String payUrl = createAlipayPayment(order, config, paymentRequest.getReturnUrl());

            return new PaymentResponseDTO(
                    true,
                    "支付创建成功",
                    payUrl,
                    null,
                    order.getOrderNo(),
                    order.getPaymentAmount(),
                    order.getPaymentType()
            );

        } catch (Exception e) {
            return new PaymentResponseDTO(
                    false,
                    "支付创建失败: " + e.getMessage(),
                    null,
                    null,
                    order.getOrderNo(),
                    order.getPaymentAmount(),
                    order.getPaymentType()
            );
        }
    }

    // 创建秒杀订单支付
    private PaymentResponseDTO createSeckillPayment(SeckillOrder seckillOrder, PaymentRequestDTO paymentRequest) {
        // 验证秒杀订单状态
        if (seckillOrder.getStatus() != 0) { // 0表示待支付
            return new PaymentResponseDTO(
                    false,
                    "订单状态异常",
                    null,
                    null,
                    seckillOrder.getOrderNo(),
                    null,
                    paymentRequest.getPaymentType()
            );
        }
        // 更新秒杀订单的支付类型
        seckillOrderMapper.updatePaymentType(seckillOrder.getId(), paymentRequest.getPaymentType());
        // 获取秒杀活动信息
        SeckillActivity activity = seckillActivityMapper.findById(seckillOrder.getSeckillId());
        if (activity == null) {
            return new PaymentResponseDTO(
                    false,
                    "秒杀活动不存在",
                    null,
                    null,
                    seckillOrder.getOrderNo(),
                    null,
                    paymentRequest.getPaymentType()
            );
        }

        // 根据支付类型获取配置
        PaymentType paymentType = paymentRequest.getPaymentType();
        PaymentConfig config = paymentConfigMapper.findByPaymentType(paymentType);
        if (config == null) {
            return new PaymentResponseDTO(
                    false,
                    "支付方式暂不支持",
                    null,
                    null,
                    seckillOrder.getOrderNo(),
                    activity.getSeckillPrice(),
                    paymentRequest.getPaymentType()
            );
        }

        try {
            // 调用支付平台API创建支付
            String payUrl = createAlipayPayment(seckillOrder, activity, config, paymentRequest.getReturnUrl());

            return new PaymentResponseDTO(
                    true,
                    "支付创建成功",
                    payUrl,
                    null,
                    seckillOrder.getOrderNo(),
                    activity.getSeckillPrice(),
                    paymentRequest.getPaymentType()
            );

        } catch (Exception e) {
            return new PaymentResponseDTO(
                    false,
                    "支付创建失败: " + e.getMessage(),
                    null,
                    null,
                    seckillOrder.getOrderNo(),
                    activity.getSeckillPrice(),
                    paymentRequest.getPaymentType()
            );
        }
    }
    
    // 支付宝支付创建（简化实现）
    private String createAlipayPayment(Order order, PaymentConfig config, String returnUrl) {
        // 实际项目中应使用支付宝SDK
        // 这里返回一个模拟的支付URL
        return "https://mockalipay.com/pay?order_no=" + order.getOrderNo() + 
               "&amount=" + order.getPaymentAmount() + "&return_url=" + returnUrl;
    }
    
    // 秒杀订单支付宝支付创建（简化实现）
    private String createAlipayPayment(SeckillOrder seckillOrder, SeckillActivity activity, PaymentConfig config, String returnUrl) {
        // 实际项目中应使用支付宝SDK
        // 这里返回一个模拟的支付URL
        return "https://mockalipay.com/pay?order_no=" + seckillOrder.getOrderNo() + 
               "&amount=" + activity.getSeckillPrice() + "&return_url=" + returnUrl;
    }
    
    @Override//  处理支付通知
    @Transactional
    public Boolean handlePaymentNotify(PaymentNotifyDTO notifyDTO) {

        // 2. 根据订单号查询订单
        Order order = orderMapper.findByOrderNo(notifyDTO.getOrderNo());
        if (order != null) {
            // 普通订单支付处理
            return handleOrderPaymentNotify(order, notifyDTO);
        }
        
        // 3. 检查是否是秒杀订单
        SeckillOrder seckillOrder = seckillOrderMapper.findByOrderNo(notifyDTO.getOrderNo());
        if (seckillOrder != null) {
            // 秒杀订单支付处理
            return handleSeckillPaymentNotify(seckillOrder, notifyDTO);
        }
        
        return false;
    }
    
    // 处理普通订单支付通知
    private Boolean handleOrderPaymentNotify(Order order, PaymentNotifyDTO notifyDTO) {
        // 验证支付金额
        if (notifyDTO.getAmount().compareTo(order.getPaymentAmount()) != 0) {
            return false;
        }

        // 更新订单状态
        if (notifyDTO.getStatus() == 1) { // 支付成功
            orderMapper.updateOrderStatus(order.getId(), order.getUserId(), OrderStatus.PAID.getCode());// 已支付
            order.setPayTime(notifyDTO.getPayTime());

            // 记录支付信息
            Payment payment = new Payment();
            payment.setOrderId(order.getId());
            payment.setPaymentNo(notifyDTO.getPaymentNo());

            // 提供默认支付类型逻辑
            PaymentType paymentType = order.getPaymentType();
            if (paymentType == null) {
                paymentType = PaymentType.WECHAT_PAY;
            }
            payment.setPaymentType(paymentType);

            payment.setAmount(notifyDTO.getAmount());
            payment.setStatus(1); // 支付成功
            payment.setPayTime(notifyDTO.getPayTime());
            paymentMapper.insert(payment);

            return true;
        }

        return false;
    }
    
    // 处理秒杀订单支付通知
    private Boolean handleSeckillPaymentNotify(SeckillOrder seckillOrder, PaymentNotifyDTO notifyDTO) {
        // 获取秒杀活动信息
        SeckillActivity activity = seckillActivityMapper.findById(seckillOrder.getSeckillId());
        if (activity == null) {
            return false;
        }

        // 验证支付金额
        if (notifyDTO.getAmount().compareTo(activity.getSeckillPrice()) != 0) {
            return false;
        }

        // 更新秒杀订单状态和支付类型
        if (notifyDTO.getStatus() == OrderStatus.PAID.getCode()) { // 支付成功
            // 获取支付记录中的支付类型
            Payment paymentRecord = paymentMapper.findByPaymentNo(notifyDTO.getPaymentNo());
            PaymentType paymentType = paymentRecord != null ? paymentRecord.getPaymentType() : null;

            // 更新秒杀订单状态和支付类型
            seckillOrderMapper.updateStatus(seckillOrder.getId(), OrderStatus.PAID.getCode());
            if (paymentType != null) {
                seckillOrderMapper.updatePaymentType(seckillOrder.getId(), paymentType);
            }

            // 记录支付信息
            Payment payment = new Payment();
            payment.setOrderId(seckillOrder.getId());
            payment.setPaymentNo(notifyDTO.getPaymentNo());
            payment.setPaymentType(paymentType);
            payment.setAmount(notifyDTO.getAmount());
            payment.setStatus(OrderStatus.PAID.getCode()); // 支付成功
            payment.setPayTime(notifyDTO.getPayTime());
            paymentMapper.insert(payment);

            return true;
        }

        return false;
    }

    @Override
    public Boolean queryPaymentStatus(String orderNo) {
        // 1. 先尝试通过普通订单号查询支付记录
        Payment payment = paymentMapper.findByOrderNo(orderNo);
        if (payment != null) {
            // 如果支付记录存在且状态为已支付，返回true
            return payment.getStatus() == OrderStatus.PAID.getCode();
        }

        // 2. 如果普通订单未找到，尝试通过秒杀订单号查询支付记录
        payment = paymentMapper.findBySeckillOrderNo(orderNo);
        if (payment != null) {
            // 如果支付记录存在且状态为已支付，返回true
            return payment.getStatus() == OrderStatus.PAID.getCode();
        }

        // 3. 订单不存在
        return false;
    }
    
    @Override
    public PaymentConfigDTO getPaymentConfig(PaymentType paymentType) {
        PaymentConfig config = paymentConfigMapper.findByPaymentType(paymentType);
        if (config == null) {
            return null;
        }
        
        PaymentConfigDTO dto = new PaymentConfigDTO();
        dto.setId(config.getId());
        dto.setPaymentType(config.getPaymentType());
        dto.setAppId(config.getAppId());
        dto.setMerchantId(config.getMerchantId());
        dto.setPublicKey(config.getPublicKey());
        dto.setPrivateKey(config.getPrivateKey());
        dto.setNotifyUrl(config.getNotifyUrl());
        dto.setIsActive(config.getIsActive());
        
        return dto;
    }
    
    @Override
    public PaymentConfigDTO updatePaymentConfig(PaymentConfigDTO configDTO) {
        PaymentConfig config = new PaymentConfig();
        config.setId(configDTO.getId());
        config.setPaymentType(configDTO.getPaymentType());
        config.setAppId(configDTO.getAppId());
        config.setMerchantId(configDTO.getMerchantId());
        config.setPublicKey(configDTO.getPublicKey());
        config.setPrivateKey(configDTO.getPrivateKey());
        config.setNotifyUrl(configDTO.getNotifyUrl());
        config.setIsActive(configDTO.getIsActive());
        
        paymentConfigMapper.update(config);
        
        return configDTO;
    }

}