package com.xiaomimall.mapper;

import com.xiaomimall.config.PaymentTypeHandler;
import com.xiaomimall.entity.PaymentType;
import com.xiaomimall.entity.SeckillOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SeckillOrderMapper {
    
    // 插入秒杀订单 - 添加 payment_type 字段
    @Insert("INSERT INTO seckill_orders (user_id, seckill_id, order_no, status, payment_type, created_at, updated_at) " +
            "VALUES (#{userId}, #{seckillId}, #{orderNo}, #{status}, #{paymentType, typeHandler=com.xiaomimall.config.PaymentTypeHandler}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeckillOrder order);
    
    // 更新秒杀订单状态
    @Update("UPDATE seckill_orders SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("UPDATE seckill_orders SET payment_type = #{paymentType, typeHandler=com.xiaomimall.config.PaymentTypeHandler}, updated_at = NOW() WHERE id = #{id}")
    int updatePaymentType(@Param("id") Long id, @Param("paymentType") PaymentType paymentType);
    // 根据用户ID和秒杀ID查询订单
    @Select("SELECT * FROM seckill_orders WHERE user_id = #{userId} AND seckill_id = #{seckillId}")
    SeckillOrder findByUserIdAndSeckillId(@Param("userId") Long userId, @Param("seckillId") Long seckillId);
    
    // 根据订单号查询订单
    @Results({
            @Result(column = "payment_type", property = "paymentType", typeHandler = PaymentTypeHandler.class)
    })
    @Select("SELECT * FROM seckill_orders WHERE order_no = #{orderNo}")
    SeckillOrder findByOrderNo(String orderNo);
    
    // 新增：根据ID查询秒杀订单
    @Select("SELECT * FROM seckill_orders WHERE id = #{id}")
    SeckillOrder findById(Long id);

    @Delete("DELETE FROM seckill_orders WHERE order_no = #{orderNo}")
    void deleteByOrderNo(String orderNo);

    @Delete("DELETE FROM seckill_orders WHERE seckill_id = #{seckillId}")
    int deleteBySeckillId(Long seckillId);
}