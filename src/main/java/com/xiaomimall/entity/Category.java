package com.xiaomimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体类
 * 用于表示商品的多级分类结构
 */
@Data
public class Category {
    private Long id;            // 分类ID
    private String name;        // 分类名称
/*表示分类的父级分类ID，用于构建分类的层级结构
值为0时表示顶级分类（没有父分类）
例如：一级分类的 parentId 为0，二级分类的 parentId 为对应一级分类的 id*/
    private Long parentId;      // 父分类ID（0表示顶级分类）
/*表示分类的层级深度（1/2/3级）
用于标识分类在层级结构中的位置
通常1级为顶级分类，2级为二级分类，3级为三级分类
便于前端展示和后端查询时区分不同层级的分类*/
    private Integer level;      // 分类层级（1/2/3级）
    private Integer sort;       // 排序值
    private Boolean isDeleted;  // 是否删除（逻辑删除）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}