package com.xiaomimall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品数据传输对象
 * 用于商品信息的请求和响应
 */
@Data
public class ProductDTO {
    private Long id;//商品ID
    @NotBlank(message = "商品名称不能为空")
    private String name;//商品名称
    private Long categoryId;//分类ID
    @DecimalMin(value = "0.0", message = "价格不能为负数")
    private BigDecimal price;
    private Integer stock;//库存
    private String description;//商品描述
    private String detail;//商品详情
    private Integer status;//商品状态
    private List<ProductImageDTO> images; // 商品图片列表

    // 嵌套的图片DTO
    @Data
    public static class ProductImageDTO {
        private String url;
        private Boolean isMain;
    }
}