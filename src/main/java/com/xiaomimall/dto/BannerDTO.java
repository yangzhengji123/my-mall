package com.xiaomimall.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 轮播图数据传输对象
 * 用于轮播图信息的请求和响应
 */
@Data
public class BannerDTO {
    @NotNull(message = "ID不能为空", groups = Update.class)
    private Long id;
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过50个字符")
    private String title;
    @NotBlank(message = "图片URL不能为空")
    @Pattern(regexp = "^https?://.*$", message = "图片URL必须以http://或https://开头")
    private String imageUrl;
    @NotNull(message = "排序权重不能为空")
    @Positive(message = "排序权重必须为正整数")
    private Integer sort;
    @NotNull(message = "启用状态不能为空")
    private Boolean isActive;
    // 移除 @Future 注解，允许设置过去、现在或未来的时间
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}