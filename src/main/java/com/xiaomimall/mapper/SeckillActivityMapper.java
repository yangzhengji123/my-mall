package com.xiaomimall.mapper;

import com.xiaomimall.entity.SeckillActivity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 秒杀活动Mapper
 * 管理秒杀活动数据
 */
@Mapper
public interface SeckillActivityMapper {
    
    // 插入秒杀活动
    @Insert("INSERT INTO seckill_activities (product_id, seckill_price, stock, start_time, end_time, is_active) " +
            "VALUES (#{productId}, #{seckillPrice}, #{stock}, #{startTime}, #{endTime}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeckillActivity activity);
    
    // 更新秒杀活动
    @Update("UPDATE seckill_activities SET seckill_price = #{seckillPrice}, stock = #{stock}, " +
            "start_time = #{startTime}, end_time = #{endTime}, is_active = #{isActive} " +
            "WHERE id = #{id}")
    int update(SeckillActivity activity);
    
    // 减少秒杀库存
    @Update("UPDATE seckill_activities SET stock = stock - #{quantity} " +
            "WHERE id = #{id} AND stock >= #{quantity}")
    int reduceStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    // 根据ID查询秒杀活动
    @Select("SELECT * FROM seckill_activities WHERE id = #{id}")
    SeckillActivity findById(Long id);
    
    // 查询所有有效的秒杀活动
    @Select("SELECT * FROM seckill_activities WHERE is_active = true AND end_time > NOW() ORDER BY start_time ASC")
    List<SeckillActivity> findActiveActivities();
    
    // 查询即将开始的秒杀活动
    @Select("SELECT * FROM seckill_activities WHERE is_active = true AND start_time > NOW() " +
            "AND start_time < DATE_ADD(NOW(), INTERVAL 24 HOUR) ORDER BY start_time ASC")
    List<SeckillActivity> findUpcomingActivities();

    @Update("UPDATE seckill_activities SET stock = #{newStock} " +
            "WHERE id = #{activityId}")
    int updateStock(@Param("activityId") Long activityId,
                    @Param("newStock") Integer newStock);

    @Delete("DELETE FROM seckill_activities WHERE id = #{id}")
    int delete(Long id);
}
