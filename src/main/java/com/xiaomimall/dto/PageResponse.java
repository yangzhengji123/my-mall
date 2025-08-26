package com.xiaomimall.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private Integer pageNum;
    private Integer pageSize;
    private Long total;      // 总记录数
    private Integer pages;   // 总页数
    private List<T> list;    // 当前页数据
}