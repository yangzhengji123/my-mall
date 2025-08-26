package com.xiaomimall.mapper;

import com.xiaomimall.entity.OrderItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单项Mapper
 * 管理订单中的商品项
 */
@Mapper
public interface OrderItemMapper {
    
    // 批量插入订单项
    @Insert("<script>" +
            "INSERT INTO order_items (order_id, product_id, product_name, product_image, price, quantity, total_price) " +
            "VALUES " +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.orderId}, #{item.productId}, #{item.productName}, #{item.productImage}, #{item.price}, #{item.quantity}, #{item.totalPrice})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("items") List<OrderItem> items);

    // 根据订单ID查询订单项
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId}")
    List<OrderItem> findByOrderId(Long orderId);
}