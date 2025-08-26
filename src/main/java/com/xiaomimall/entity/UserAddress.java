package com.xiaomimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址实体
 * 存储用户的收货地址信息
 */
@Data
public class UserAddress {
    private Long id;
    private Long userId;
    private String recipient;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}