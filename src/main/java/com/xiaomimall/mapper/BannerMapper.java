package com.xiaomimall.mapper;

import com.xiaomimall.entity.Banner;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 轮播图Mapper接口
 * 提供轮播图的管理
 */
@Mapper
public interface BannerMapper {
    
    // 插入轮播图
    @Insert("INSERT INTO banners (title, image_url, link_url, sort, is_active, start_time, end_time) " +
            "VALUES (#{title}, #{imageUrl}, #{linkUrl}, #{sort}, #{isActive}, #{startTime}, #{endTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Banner banner);
    
    // 更新轮播图
    @Update("UPDATE banners SET title = #{title}, image_url = #{imageUrl}, link_url = #{linkUrl}, " +
            "sort = #{sort}, is_active = #{isActive}, start_time = #{startTime}, end_time = #{endTime} " +
            "WHERE id = #{id}")
    int update(Banner banner);
    
    // 删除轮播图
    @Delete("DELETE FROM banners WHERE id = #{id}")
    int deleteById(Long id);
    
    // 根据ID查询轮播图
    @Select("SELECT * FROM banners WHERE id = #{id}")
    Banner findById(Long id);
    
    // 查询所有启用的轮播图（按排序）
    @Select("SELECT * FROM banners WHERE is_active = 1 " +
            "AND (start_time IS NULL OR start_time <= NOW()) " +
            "AND (end_time IS NULL OR end_time >= NOW()) " +
            "ORDER BY sort ASC")
    List<Banner> findActiveBanners();
}