package com.xiaomimall.dto;

import lombok.Data;

import java.util.List;

/**
 * 分类数据传输对象
 * 用于分类信息的请求和响应
 */
@Data
public class CategoryDTO {
    private Long id;//分类ID
    private String name;//分类名称
    private Long parentId;//指向父类id
    private Integer level;//分类层级
    private Integer sort;       // 排序值
    private String icon;       // 分类图标
    private String description; // 分类描述
    private Boolean isActive;  // 是否启用
    private List<CategoryDTO> children; // 子分类列表
}