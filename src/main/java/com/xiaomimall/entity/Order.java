package com.xiaomimall.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 * 代表用户的订单信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long id;//  订单ID
    private String orderNo;        // 订单号
    private Long userId;           //  用户ID
    private BigDecimal totalAmount; // 订单总金额
    private BigDecimal paymentAmount; // 实际支付金额
    private PaymentType paymentType;  // 支付方式
    private Integer status;        // 订单状态
    private Long addressId;        // 收货地址ID
    private String remark;         // 订单备注
    private LocalDateTime payTime;      // 支付时间
    private LocalDateTime deliveryTime;  // 发货时间
    private LocalDateTime completeTime; // 完成时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 添加枚举辅助方法
    /**
     * 获取订单状态枚举
     * @return 订单状态枚举
     */
    public OrderStatus getStatusEnum() {
        return OrderStatus.fromCode(this.status);
    }
    /**
     * 设置订单状态
     * @param status 订单状态枚举
     */
    public void setStatusEnum(OrderStatus status) {
        this.status = status.getCode();
    }
}
