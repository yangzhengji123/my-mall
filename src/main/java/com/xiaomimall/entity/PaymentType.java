// PaymentType.java
package com.xiaomimall.entity;

import lombok.Getter;

@Getter
public enum PaymentType {
    ALIPAY(1, "支付宝"),
    WECHAT_PAY(2, "微信支付"),
    BANK_TRANSFER(3, "银行转账"),
    CASH_ON_DELIVERY(4, "货到付款");

    private final int code;
    private final String description;

    PaymentType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentType fromCode(Integer code) {
        if (code == null) {
            return null;
        }

        for (PaymentType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的支付方式码: " + code);
    }
}