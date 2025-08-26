package com.xiaomimall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品查询参数DTO
 * 用于商品搜索和筛选
 */
@Data
public class ProductQueryDTO {
    private String keyword;     // 搜索关键词
    private Long categoryId;    // 分类ID
    private BigDecimal minPrice; // 最低价格
    private BigDecimal maxPrice; // 最高价格
    private Integer status;      // 商品状态
    private String sortBy;       // 排序字段（price/sales）
    private String sortOrder;    // 排序方向（asc/desc）
    private Integer pageNum = 1; // 页码
    @Min(value = 1, message = "每页数量至少为1")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 10; // 每页数量
}