package com.xiaomimall.mapper;

import com.xiaomimall.entity.Order;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    // 插入订单
    @Insert("INSERT INTO orders (order_no, user_id, total_amount, payment_amount, payment_type, status, address_id, remark) " +
            "VALUES (#{orderNo}, #{userId}, #{totalAmount}, #{paymentAmount}, #{paymentType}, #{status}, #{addressId}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    // 更新订单状态
    @Update("UPDATE orders SET status = #{status} WHERE id = #{id} AND user_id = #{userId}")
    int updateOrderStatus(@Param("id") Long id, @Param("userId") Long userId, @Param("status") int status);
    
    // 更新订单状态并设置物流信息
    @Update("UPDATE orders SET status = #{status}, tracking_number = #{trackingNumber}, delivered_at = #{deliveredAt} WHERE id = #{orderId}")
    int updateOrderStatusWithTracking(
            @Param("orderId") Long orderId,
            @Param("status") int status,
            @Param("trackingNumber") String trackingNumber,
            @Param("deliveredAt") LocalDateTime deliveredAt);
    
    // 更新订单状态并设置完成时间
    @Update("UPDATE orders SET status = #{status}, completed_at = #{completedAt} WHERE id = #{orderId}")
    int updateOrderStatusWithCompleteTime(
            @Param("orderId") Long orderId,
            @Param("status") int status,
            @Param("completedAt") LocalDateTime completedAt);

    // 根据ID查询订单
    @Select("SELECT * FROM orders WHERE id = #{orderId}")
    Order findById(Long orderId);

    // 根据订单号查询订单
    @Select("SELECT * FROM orders WHERE order_no = #{orderNo}")
    Order findByOrderNo(String orderNo);

    // 查询用户的订单列表
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Order> findByUserId(Long userId);

    @Select("SELECT * FROM orders WHERE id = #{orderId} AND user_id = #{id}")
    Order findByIdAndUserId(Long orderId, Long id);
}