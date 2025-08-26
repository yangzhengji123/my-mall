package com.xiaomimall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 秒杀结果DTO
 * 返回秒杀操作的结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillResultDTO {
    private Boolean success;
    private String message;
    private String orderNo; // 秒杀成功时返回订单号
    private Long waitTime; // 等待时间（毫秒）
}