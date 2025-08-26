package com.xiaomimall.service;

import com.xiaomimall.dto.*;
import com.xiaomimall.entity.*;
import com.xiaomimall.exception.InvalidAddressException;
import com.xiaomimall.exception.InvalidCartException;
import com.xiaomimall.exception.NotFoundException;
import com.xiaomimall.exception.OutOfStockException;
import com.xiaomimall.mapper.*;
import com.xiaomimall.util.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final PaymentMapper paymentMapper;
    private final ProductImageMapper productImageMapper;

    // 移除了原有的硬编码常量定义

    // 创建订单
    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, OrderCreateDTO orderCreateDTO) {
        // 1. 验证收货地址
        UserAddress address = addressMapper.findById(orderCreateDTO.getAddressId(), userId);
        if (address == null) {
            throw new InvalidAddressException("收货地址无效或不存在");
        }

        // 2. 验证前端传入的商品项
        List<CartItemDTO> selectedItems = orderCreateDTO.getItems();
        if (selectedItems == null || selectedItems.isEmpty()) {
            throw new InvalidCartException("未选择任何商品");
        }

        // 3. 验证库存并锁定
        List<Product> products = validateAndLockStock(selectedItems);

        try {
            // 4. 计算订单总金额
            BigDecimal totalAmount = calculateTotalAmount(selectedItems, products);

            // 5. 创建订单
            Order order = new Order();
            order.setOrderNo(OrderNoGenerator.generateOrderNo());
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setPaymentAmount(totalAmount); // 实际支付金额（暂不考虑优惠）
            order.setPaymentType( orderCreateDTO.getPaymentType());
            order.setStatus(OrderStatus.PENDING.getCode());
            order.setAddressId(address.getId());
            order.setRemark(orderCreateDTO.getRemark());

            // 6. 插入订单
            orderMapper.insert(order);

            // 7. 创建订单项
            List<OrderItem> orderItems = createOrderItems(order.getId(), selectedItems, products);
            orderItemMapper.insertBatch(orderItems);

            // 8. 返回订单详情
            return convertToOrderDTO(order, orderItems, address);

        } catch (Exception e) {
            // 发生异常时释放库存
            releaseStock(selectedItems);
            throw e;
        }
    }


    // 计算订单总金额（保持不变）
    private BigDecimal calculateTotalAmount(List<CartItemDTO> items, List<Product> products) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        BigDecimal total = BigDecimal.ZERO;
        for (CartItemDTO item : items) {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }
        return total;
    }
    
    // 验证库存并锁定（保持不变）
    private List<Product> validateAndLockStock(List<CartItemDTO> items) {
        List<Product> products = new ArrayList<>();
        
        for (CartItemDTO item : items) {
            Product product = productMapper.findById(item.getProductId());
            if (product == null || product.getStatus() != 1) {
                throw new NotFoundException("商品不存在或已下架: " + item.getProductId());
            }
            
            if (product.getStock() < item.getQuantity()) {
                throw new OutOfStockException("商品库存不足: " + product.getName());
            }
            
            // 锁定库存（减少库存）
            int updated = productMapper.reduceStock(product.getId(), item.getQuantity());
            if (updated == 0) {
                throw new OutOfStockException("商品库存不足: " + product.getName());
            }
            
            products.add(product);
        }
        
        return products;
    }
    
    // 释放库存（回滚操作）（保持不变）
    private void releaseStock(List<CartItemDTO> items) {
        for (CartItemDTO item : items) {
            productMapper.increaseStock(item.getProductId(), item.getQuantity());
        }
    }
    
    // 创建订单项（保持不变）
    private List<OrderItem> createOrderItems(Long orderId, List<CartItemDTO> cartItems, List<Product> products) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CartItemDTO item : cartItems) {
            Product product = productMap.get(item.getProductId());
            if (product == null) continue;
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(getMainImage(product.getId())); // 获取主图
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            
            orderItems.add(orderItem);
        }
        
        return orderItems;
    }
    
    // 获取商品主图（保持不变）
    private String getMainImage(Long productId) {
        // 参数校验
        if (productId == null) {
            return "https://cdn.example.com/default-product.jpg";
        }

        try {
            // 优先查询主图
            ProductImage mainImage = productImageMapper.findMainImageByProductId(productId);

            // 如果没有主图，则获取第一张图片作为降级方案
            if (mainImage == null) {
                List<ProductImage> images = productImageMapper.findByProductId(productId);
                if (!images.isEmpty()) {
                    mainImage = images.getFirst(); // 获取第一张图片
                }
            }
            if (mainImage != null && mainImage.getUrl() != null && !mainImage.getUrl().trim().isEmpty()) {
                return mainImage.getUrl();
            }
        } catch (Exception e) {log.error("获取商品主图时发生异常，商品ID：{}", productId, e);
        }

        // 返回默认图片
        return "https://cdn.example.com/default-product.jpg";
    }
    
    // 取消订单
    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        // 查询订单并验证归属
        Order order = orderMapper.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }

        if (order.getStatus().intValue() != OrderStatus.PENDING.getCode()) {
            throw new IllegalStateException("订单状态为：" + OrderStatus.fromCode(order.getStatus()).getDescription() + "，只能取消待支付订单");
        }

       int rowsAffected = orderMapper.updateOrderStatus(orderId, userId, OrderStatus.CANCELLED.getCode());
        if (rowsAffected == 0) {
            throw new IllegalStateException("订单取消失败，可能已被其他操作修改");
        }

        List<OrderItem> orderItems = orderItemMapper.findByOrderId(orderId);
        if (orderItems != null && !orderItems.isEmpty()) {
            for (OrderItem item : orderItems) {
                productMapper.increaseStock(item.getProductId(), item.getQuantity());
            }
        }
    }


    // 获取订单详情（保持不变）
    @Override
    public OrderDTO getOrderDetail(Long userId, Long orderId) {
        Order order = orderMapper.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }

        // 获取订单地址
        UserAddress address = addressMapper.findById(order.getAddressId(), userId);

        // 地址不存在时的处理逻辑
        if (address == null) {
            throw new InvalidAddressException("收货地址无效或不存在");
        }

        // 获取订单项
        List<OrderItem> orderItems = orderItemMapper.findByOrderId(orderId);
        if (orderItems == null) {
            orderItems = Collections.emptyList();
        }

        return convertToOrderDTO(order, orderItems, address);
    }


    // 获取用户订单列表（保持不变）
    @Override
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderMapper.findByUserId(userId);
        return orders.stream().map(order -> {
            // 为了简化，这里不查询每个订单的地址和订单项
            return convertToOrderDTO(order, null, null);
        }).collect(Collectors.toList());
    }

    // 支付订单
    @Override
    @Transactional
    public void payOrder(Long userId, Long orderId) {
        Order order = orderMapper.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }

        if (order.getStatus().intValue() != OrderStatus.PENDING.getCode()) {
            throw new IllegalStateException("订单状态为：" + OrderStatus.fromCode(order.getStatus()).getDescription() + "，无法支付");
        }

        int rowsAffected = orderMapper.updateOrderStatus(orderId, userId, OrderStatus.PAID.getCode());
        if (rowsAffected == 0) {
            throw new IllegalStateException("支付失败，订单状态已被其他操作修改");
        }

      String paymentNo = OrderNoGenerator.generateOrderNo();

        // 记录支付信息
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentNo(paymentNo);
        payment.setPaymentType(order.getPaymentType());
        payment.setAmount(order.getPaymentAmount());
        payment.setStatus(1); // 支付成功
        payment.setPayTime(LocalDateTime.now());
        paymentMapper.insert(payment);
    }


    // 发货订单
    @Override
    @Transactional
    public void deliverOrder(Long orderId, String trackingNumber) {
        // 实际项目中需要验证管理员权限
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }

        // 状态检查（必须为已支付）
        if (order.getStatus() != OrderStatus.PAID.getCode()) {
            throw new IllegalStateException("订单状态为: " + OrderStatus.fromCode(order.getStatus()).getDescription() + "，只能对已支付订单发货");
        }

        // 更新订单状态为已发货，并保存物流单号和发货时间
        LocalDateTime now = LocalDateTime.now();
        orderMapper.updateOrderStatusWithTracking(
                orderId,
                OrderStatus.SHIPPED.getCode(),
                trackingNumber,
                now
        );
    }


    // 完成订单
    @Override
    @Transactional
    public void completeOrder(Long userId, Long orderId) {
        // 验证订单是否属于当前用户
        if (!isOrderBelongToUser(orderId, userId)) {
            throw new NotFoundException("订单不存在或无权操作");
        }

        Order order = orderMapper.findByIdAndUserId(orderId, userId);
        if (order == null) {
            throw new NotFoundException("订单不存在");
        }

        // 状态检查（必须为已发货）
        if (order.getStatus() != OrderStatus.SHIPPED.getCode()) {
            throw new IllegalStateException("订单状态为: " + OrderStatus.fromCode(order.getStatus()).getDescription() + "，只能对已发货订单完成");
        }

        // 更新订单状态为已完成，并保存完成时间
        LocalDateTime now = LocalDateTime.now();
        orderMapper.updateOrderStatusWithCompleteTime(
                orderId,
                OrderStatus.COMPLETED.getCode(),
                now
        );
    }



    @Override
    public boolean isOrderBelongToUser(Long orderId, Long userId) {
        Order order = orderMapper.findByIdAndUserId(orderId, userId);
        return order != null;
    }


    // 转换订单实体为DTO（保持不变）
    private OrderDTO convertToOrderDTO(Order order, List<OrderItem> orderItems, UserAddress address) {
        if (orderItems == null) {
            orderItems = Collections.emptyList();
        }
        OrderDTO dto = new OrderDTO();
        dto.setOrderNo(order.getOrderNo());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setPaymentAmount(order.getPaymentAmount());
        dto.setPaymentType(order.getPaymentType());
        // 设置支付方式描述
        if (order.getPaymentType() != null) {
            dto.setPaymentType(order.getPaymentType());
            try {
                dto.setPaymentTypeDescription(PaymentType.fromCode(order.getPaymentType().getCode()).getDescription());
            } catch (IllegalArgumentException e) {
                dto.setPaymentTypeDescription("未知支付方式");
            }
        } else {
            dto.setPaymentTypeDescription("支付方式未设置");
        }
        // 使用枚举设置状态描述
        dto.setStatus(order.getStatus());
        try {
            dto.setStatusDescription(OrderStatus.fromCode(order.getStatus()).getDescription());
        } catch (IllegalArgumentException e) {
            dto.setStatusDescription("未知状态");
        }
        dto.setRemark(order.getRemark());
        dto.setCreatedAt(order.getCreatedAt());

        // 设置地址DTO（保持不变）
        if (address != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setId(address.getId());
            addressDTO.setRecipient(address.getRecipient());
            addressDTO.setPhone(address.getPhone());
            addressDTO.setProvince(address.getProvince());
            addressDTO.setCity(address.getCity());
            addressDTO.setDistrict(address.getDistrict());
            addressDTO.setDetail(address.getDetail());
            addressDTO.setIsDefault(address.getIsDefault());
            dto.setAddress(addressDTO);
        }

        // 设置订单项DTO（保持不变）
        if (orderItems != null && !orderItems.isEmpty()) {
            List<OrderDTO.OrderItemDTO> itemDTOs = orderItems.stream().map(item -> {
                OrderDTO.OrderItemDTO itemDTO = new OrderDTO.OrderItemDTO();
                itemDTO.setProductId(item.getProductId());
                itemDTO.setProductName(item.getProductName());
                itemDTO.setProductImage(item.getProductImage());
                itemDTO.setPrice(item.getPrice());
                itemDTO.setQuantity(item.getQuantity());
                itemDTO.setTotalPrice(item.getTotalPrice());
                return itemDTO;
            }).collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }
}
