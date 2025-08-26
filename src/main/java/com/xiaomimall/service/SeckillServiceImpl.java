package com.xiaomimall.service;

import com.xiaomimall.dto.ProductDTO;
import com.xiaomimall.dto.SeckillActivityDTO;
import com.xiaomimall.dto.SeckillResultDTO;
import com.xiaomimall.entity.*;
import com.xiaomimall.exception.SeckillException;
import com.xiaomimall.mapper.ProductMapper;
import com.xiaomimall.mapper.SeckillActivityMapper;
import com.xiaomimall.mapper.SeckillOrderMapper;
import com.xiaomimall.util.OrderNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 秒杀服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {
    
    private final SeckillActivityMapper seckillActivityMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;


    // Redis键前缀
    private static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
    private static final String SECKILL_USER_PREFIX = "seckill:user:";
    private static final String SECKILL_ACTIVITY_PREFIX = "seckill:activity:";
    
    // Lua脚本：原子性执行秒杀操作
    private static final String SECKILL_SCRIPT =
        "local stockKey = KEYS[1] " +
        "local userKey = KEYS[2] " +
        "local userId = ARGV[1] " +
        "local quantity = tonumber(ARGV[2]) " +
        "local activityId = ARGV[3] " +
        " " +
        "local stock = redis.call('get', stockKey) " +
        "if not stock or tonumber(stock) < quantity then " +
        "    return 0 " +
        "end " +
        " " +
        "if redis.call('sismember', userKey, userId) == 1 then " +
        "    return 2 " +
        "end " +
        " " +
        "redis.call('decrby', stockKey, quantity) " +
        "redis.call('sadd', userKey, userId) " +
        "redis.call('expire', userKey, 86400) " + // 24小时过期
        "return 1 ";

    @Override
    @Transactional
    public SeckillResultDTO executeSeckill(Long userId, Long seckillId,PaymentType paymentType) {
        // 1. 验证秒杀活动
        SeckillActivity activity = seckillActivityMapper.findById(seckillId);
        if (activity == null || !activity.getIsActive()) {
            return new SeckillResultDTO(false, "秒杀活动不存在", null, 0L);
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            long waitTime = java.time.Duration.between(now, activity.getStartTime()).toMillis();
            return new SeckillResultDTO(false, "秒杀尚未开始", null, waitTime);
        }
        
        if (now.isAfter(activity.getEndTime())) {
            return new SeckillResultDTO(false, "秒杀已结束", null, 0L);
        }
        
        // 2. 检查用户是否已经参与过秒杀
        SeckillOrder existingOrder = seckillOrderMapper.findByUserIdAndSeckillId(userId, seckillId);
        if (existingOrder != null) {
            return new SeckillResultDTO(false, "您已经参与过本次秒杀", null, 0L);
        }
        
        // 3. 使用Redis Lua脚本原子性执行秒杀操作
        String stockKey = SECKILL_STOCK_PREFIX + seckillId;
        String userKey = SECKILL_USER_PREFIX + seckillId;
        
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(SECKILL_SCRIPT, Long.class);
        Long result = redisTemplate.execute(redisScript, 
            Arrays.asList(stockKey, userKey),
            userId.toString(), "1", seckillId.toString());
        
        // 4. 处理秒杀结果
        if (result == null || result == 0) {
            return new SeckillResultDTO(false, "秒杀失败，库存不足", null, 0L);
        } else if (result == 2) {
            return new SeckillResultDTO(false, "您已经参与过本次秒杀", null, 0L);
        }

        // 修改后的try-catch块
        try {
            // 5. 秒杀成功，创建秒杀订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setUserId(userId);
            seckillOrder.setSeckillId(seckillId);
            seckillOrder.setOrderNo(OrderNoGenerator.generateOrderNo());
            seckillOrder.setStatus(0);
            seckillOrderMapper.insert(seckillOrder); // 写入数据库订单

            // 6. 更新数据库库存 (如果此步失败，下面的catch会回滚Redis和订单)
            int updateCount = seckillActivityMapper.reduceStock(seckillId, 1);
            if (updateCount == 0) {
                // 极端情况：库存扣减为0，说明已经卖完，需要回滚
                throw new RuntimeException("Database stock reduction failed");
            }

            return new SeckillResultDTO(true, "秒杀成功", seckillOrder.getOrderNo(), 0L);

        } catch (Exception e) {
            // 【关键修改】回滚Redis前，先尝试删除可能已经创建的数据库订单
            SeckillOrder seckillOrder = seckillOrderMapper.findByOrderNo(OrderNoGenerator.generateOrderNo());
            if (seckillOrder != null && seckillOrder.getOrderNo() != null) {
                try {
                    seckillOrderMapper.deleteByOrderNo(seckillOrder.getOrderNo());
                } catch (Exception ex) {
                    log.error("回滚删除订单失败，需要人工干预！orderNo: {}", seckillOrder.getOrderNo(), ex);
                }
            }
            // 回滚Redis库存和用户记录
            redisTemplate.opsForValue().increment(stockKey, 1);
            redisTemplate.opsForSet().remove(userKey, userId.toString());

            log.error("秒杀流程异常，已回滚: userId={}, seckillId={}", userId, seckillId, e);
            return new SeckillResultDTO(false, "系统异常，请重试", null, 0L);
        }
    }
    
    @Override
    public List<SeckillActivityDTO> getActiveSeckills() {
        List<SeckillActivity> activities = seckillActivityMapper.findActiveActivities();
        return convertToActivityDTOs(activities);
    }
    
    @Override
    public List<SeckillActivityDTO> getUpcomingSeckills() {
        List<SeckillActivity> activities = seckillActivityMapper.findUpcomingActivities();
        return convertToActivityDTOs(activities);
    }

    @Override
    @Transactional
    public SeckillActivityDTO createSeckillActivity(SeckillActivityDTO activityDTO) {
        // 验证商品是否存在
        Product product = productMapper.findById(activityDTO.getProductId());
        if (product == null) {
            throw new SeckillException("商品不存在");
        }
        // 创建秒杀活动
        SeckillActivity activity = new SeckillActivity();
        activity.setProductId(activityDTO.getProductId());
        activity.setSeckillPrice(activityDTO.getSeckillPrice());
        activity.setStock(activityDTO.getStock());
        activity.setStartTime(activityDTO.getStartTime());
        activity.setEndTime(activityDTO.getEndTime());
        activity.setIsActive(activityDTO.getIsActive());
        seckillActivityMapper.insert(activity);
        
        // 初始化Redis库存
        String stockKey = SECKILL_STOCK_PREFIX + activity.getId();
        redisTemplate.opsForValue().set(stockKey, activity.getStock());
        // 设置Redis键过期时间（活动结束后24小时）
        long expireSeconds = java.time.Duration.between(LocalDateTime.now(), activity.getEndTime())
                .plusHours(24).getSeconds();
// 确保过期时间至少为1秒
        if (expireSeconds <= 0) {
            expireSeconds = 24 * 60 * 60; // 默认24小时
        }
        redisTemplate.expire(stockKey, expireSeconds, java.util.concurrent.TimeUnit.SECONDS);
        
        return convertToActivityDTO(activity, product);
    }
    
    @Override
    @Transactional
    public SeckillActivityDTO updateSeckillActivity(Long id, SeckillActivityDTO activityDTO) {
        SeckillActivity activity = seckillActivityMapper.findById(id);
        if (activity == null) {
            throw new SeckillException("秒杀活动不存在");
        }
        
        // 更新秒杀活动
        activity.setSeckillPrice(activityDTO.getSeckillPrice());
        activity.setStock(activityDTO.getStock());
        activity.setStartTime(activityDTO.getStartTime());
        activity.setEndTime(activityDTO.getEndTime());
        activity.setIsActive(activityDTO.getIsActive());
        
        seckillActivityMapper.update(activity);
        
        // 更新Redis库存
        String stockKey = SECKILL_STOCK_PREFIX + id;
        redisTemplate.opsForValue().set(stockKey, activity.getStock());
        Product product = productMapper.findById(activity.getProductId());
        if (!activity.getStock().equals(activityDTO.getStock())) {
            return updateSeckillStock(id, activityDTO.getStock());
        }
        return convertToActivityDTO(activity, product);
    }
    @Override
    @Transactional
    public SeckillActivityDTO updateSeckillStock(Long activityId, Integer newStock) {
        // 1. 验证活动是否存在
        SeckillActivity activity = seckillActivityMapper.findById(activityId);
        if (activity == null) {
            throw new SeckillException("秒杀活动不存在");
        }

        // 2. 更新数据库库存
        int updateCount = seckillActivityMapper.updateStock(activityId, newStock);
        if (updateCount == 0) {
            throw new SeckillException("库存更新失败");
        }

        // 3. 同步更新Redis库存
        String stockKey = SECKILL_STOCK_PREFIX + activityId;
        redisTemplate.opsForValue().set(stockKey, newStock);

        // 4. 重新计算过期时间（活动结束时间+24小时）
        long expireSeconds = java.time.Duration.between(
                LocalDateTime.now(),
                activity.getEndTime()
        ).plusHours(24).getSeconds();

        if (expireSeconds > 0) {
            redisTemplate.expire(stockKey, expireSeconds, TimeUnit.SECONDS);
        }

        // 5. 返回更新后的活动信息
        return convertToActivityDTO(activity, productMapper.findById(activity.getProductId()));
    }
    @Override
    @Transactional
    public void deleteSeckillActivity(Long id) {
        // 1. 验证活动是否存在
        SeckillActivity activity = seckillActivityMapper.findById(id);
        if (activity == null) {
            throw new SeckillException("秒杀活动不存在");
        }

        // 2. 删除数据库记录（先删除订单关联数据，再删除活动）
        // 注意：实际生产环境应先处理关联订单，这里假设没有外键约束
        seckillOrderMapper.deleteBySeckillId(id);  // 删除关联订单

        // 3. 删除秒杀活动主记录
        int rowsAffected = seckillActivityMapper.delete(id);
        if (rowsAffected == 0) {
            throw new SeckillException("删除秒杀活动失败");
        }

        // 4. 清理Redis缓存
        String stockKey = SECKILL_STOCK_PREFIX + id;
        String userKey = SECKILL_USER_PREFIX + id;
        String activityKey = SECKILL_ACTIVITY_PREFIX + id;

        redisTemplate.delete(Arrays.asList(stockKey, userKey, activityKey));

    }
    
    // 转换秒杀活动列表为DTO
    private List<SeckillActivityDTO> convertToActivityDTOs(List<SeckillActivity> activities) {
        // 提取所有商品ID
        List<Long> productIds = activities.stream()
                .map(SeckillActivity::getProductId)
                .collect(Collectors.toList());

        // 批量查询商品
        List<Product> products = productMapper.findByIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 转换DTO
        return activities.stream()
                .map(activity -> {
                    Product product = productMap.get(activity.getProductId());
                    return product != null ? convertToActivityDTO(activity, product) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    // 转换单个秒杀活动为DTO
    private SeckillActivityDTO convertToActivityDTO(SeckillActivity activity, Product product) {
        SeckillActivityDTO dto = new SeckillActivityDTO();
        dto.setId(activity.getId());
        dto.setProductId(activity.getProductId());
        dto.setSeckillPrice(activity.getSeckillPrice());
        dto.setStock(activity.getStock());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setIsActive(activity.getIsActive());
        
        // 设置商品信息
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setPrice(product.getPrice());
        productDTO.setDescription(product.getDescription());
        dto.setProduct(productDTO);
        
        return dto;
    }
}
