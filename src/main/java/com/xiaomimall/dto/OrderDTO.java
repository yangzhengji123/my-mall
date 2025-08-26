package com.xiaomimall.dto;

import com.xiaomimall.entity.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单响应DTO
 * 返回给前端的订单信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal paymentAmount;
    private PaymentType paymentType;
    private String paymentTypeDescription;
    private Integer status;
    private AddressDTO address;
    private String remark;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
    private String statusDescription;

    @Data
    public static class OrderItemDTO {
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal totalPrice;
    }



    public void setStatusDescription(String statusDescription) {

        this.statusDescription = statusDescription;
    }
}