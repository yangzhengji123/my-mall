package com.xiaomimall.mapper;

import com.xiaomimall.entity.Payment;
import org.apache.ibatis.annotations.*;

@Mapper
public interface PaymentMapper {
    // 插入支付信息
    @Insert("INSERT INTO payments (order_id, payment_no, payment_type, amount, status, pay_time, created_at, updated_at) " +
            "VALUES (#{orderId}, #{paymentNo}, #{paymentType, typeHandler=com.xiaomimall.config.PaymentTypeHandler}, #{amount}, #{status}, #{payTime}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Payment payment);

    @Select("SELECT * FROM payments WHERE payment_no = #{paymentNo}")
    Payment findByPaymentNo(String paymentNo);

    @Select("SELECT p.* FROM payments p INNER JOIN seckill_orders s ON p.order_id = s.id WHERE s.order_no = #{orderNo}")
    Payment findBySeckillOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT p.* FROM payments p INNER JOIN orders o ON p.order_id = o.id WHERE o.order_no = #{orderNo}")
    Payment findByOrderNo(@Param("orderNo") String orderNo);
}