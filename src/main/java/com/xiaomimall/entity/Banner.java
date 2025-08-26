package com.xiaomimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 轮播图实体类
 * 用于管理首页轮播图展示
 */
@Data
public class Banner {
    private Long id;            // 轮播图ID
    private String title;       // 轮播图标题
    private String imageUrl;    // 图片URL
    private String linkUrl;     // 跳转链接
    private Integer sort;       // 排序值
    private Boolean isActive;   // 是否启用
    private LocalDateTime startTime; // 展示开始时间
    private LocalDateTime endTime;   // 展示结束时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}