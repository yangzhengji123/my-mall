package com.xiaomimall.service;

import com.xiaomimall.dto.OrderCreateDTO;
import com.xiaomimall.dto.OrderDTO;

import java.util.List;

/**
 * 订单服务接口
 * 定义订单相关操作
 */
public interface OrderService {
    OrderDTO createOrder(Long userId, OrderCreateDTO orderCreateDTO);//  创建订单
    void cancelOrder(Long userId, Long orderId);//  取消订单
    OrderDTO getOrderDetail(Long userId, Long orderId);//  获取订单详情
    List<OrderDTO> getUserOrders(Long userId);//  获取用户订单
    void payOrder(Long userId, Long orderId);//  支付订单
    void deliverOrder(Long orderId, String trackingNumber);//  发货
    void completeOrder(Long userId, Long orderId);//  完成订单
    boolean isOrderBelongToUser(Long orderId, Long userId);
}
