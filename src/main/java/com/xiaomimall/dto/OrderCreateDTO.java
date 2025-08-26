package com.xiaomimall.dto;

import com.xiaomimall.entity.PaymentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单创建DTO
 * 用于创建订单的请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateDTO {
    @NotNull(message = "地址ID不能为空")
    private Long addressId;//地址ID

    @NotNull(message = "支付方式不能为空")
    @Min(value = 1, message = "支付方式不合法")
    private PaymentType paymentType;//支付方式

    private String remark;//订单备注

    @NotEmpty(message = "订单商品项不能为空")
    private List<CartItemDTO> items;//订单商品项

}
