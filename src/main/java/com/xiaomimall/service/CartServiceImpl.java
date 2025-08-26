package com.xiaomimall.service;

import com.xiaomimall.dto.CartItemDTO;
import com.xiaomimall.dto.CartResponseDTO;
import com.xiaomimall.entity.Product;
import com.xiaomimall.exception.NotFoundException;
import com.xiaomimall.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 购物车服务实现（Redis）
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductMapper productMapper;
    
    // Redis键前缀
    private static final String CART_KEY_PREFIX = "cart:";
    private static final String PRODUCT_CACHE_PREFIX = "product:cache:";
    
    // 购物车有效期（7天）
    private static final long CART_EXPIRE_DAYS = 7;
    // 商品缓存有效期（1小时）
    private static final long PRODUCT_CACHE_EXPIRE_HOURS = 1;
    
    // Lua脚本：原子性添加商品到购物车
    private static final String ADD_TO_CART_SCRIPT = 
        "local cartKey = KEYS[1] " +
        "local productId = ARGV[1] " +
        "local quantity = tonumber(ARGV[2]) " +
        "local currentQuantity = redis.call('HGET', cartKey, productId) " +
        "local newQuantity = quantity " +
        "if currentQuantity then " +
        "  newQuantity = tonumber(currentQuantity) + quantity " +
        "end " +
        "redis.call('HSET', cartKey, productId, newQuantity) " +
        "redis.call('EXPIRE', cartKey, 7 * 24 * 60 * 60) " +  // 7天过期
        "return newQuantity";

    // Lua脚本：原子性更新购物车商品数量
    private static final String UPDATE_CART_ITEM_SCRIPT = 
        "local cartKey = KEYS[1] " +
        "local productId = ARGV[1] " +
        "local quantity = tonumber(ARGV[2]) " +
        "local exists = redis.call('HEXISTS', cartKey, productId) " +
        "if exists == 0 then " +
        "  return 0 " +  // 商品不存在
        "end " +
        "redis.call('HSET', cartKey, productId, quantity) " +
        "return 1";     // 更新成功

    @Override
    public void addToCart(Long userId, CartItemDTO item) {
        String cartKey = getCartKey(userId);

        // 检查商品是否存在
        Product product = productMapper.findById(item.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new NotFoundException("商品不存在或已下架");
        }

        // 使用原子操作添加商品到购物车
        redisTemplate.opsForHash().increment(cartKey, item.getProductId().toString(), item.getQuantity());

        // 设置购物车过期时间
        redisTemplate.expire(cartKey, CART_EXPIRE_DAYS, TimeUnit.DAYS);
    }
    
    @Override
    public void updateCartItem(Long userId, CartItemDTO item) {
        String cartKey = getCartKey(userId);
        
        // 检查商品是否存在并缓存商品信息
        Product product = getCachedProduct(item.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new NotFoundException("商品不存在或已下架");
        }
        
        // 使用Lua脚本原子性更新购物车商品数量
        RedisScript<Long> script = new DefaultRedisScript<>(UPDATE_CART_ITEM_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(cartKey), 
                                          item.getProductId().toString(), item.getQuantity().toString());
        
        if (result == 0) {
            throw new NotFoundException("购物车中不存在该商品");
        }
    }
    
    @Override
    public void removeCartItem(Long userId, Long productId) {
        String cartKey = getCartKey(userId);
        redisTemplate.opsForHash().delete(cartKey, productId.toString());
    }
    
    @Override
    public void clearCart(Long userId) {
        String cartKey = getCartKey(userId);
        redisTemplate.delete(cartKey);
    }

    @Override
    public CartResponseDTO getCart(Long userId) {
        String cartKey = getCartKey(userId);
        Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(cartKey);

        if (cartMap == null || cartMap.isEmpty()) {
            return new CartResponseDTO(Collections.emptyList(), BigDecimal.ZERO, 0);
        }

        // 构建购物车项并清理无效商品
        List<CartItemDTO> items = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalItems = 0;
        List<String> validProductKeys = new ArrayList<>();

        for (Map.Entry<Object, Object> entry : cartMap.entrySet()) {
            Long productId = Long.parseLong(entry.getKey().toString());
            Integer quantity = Integer.parseInt(entry.getValue().toString());

            // 从缓存中获取商品信息
            Product product = getCachedProduct(productId);
            
            // 检查商品是否存在且已上架
            if (product != null && product.getStatus() == 1) {
                CartItemDTO item = new CartItemDTO();
                item.setProductId(productId);
                item.setQuantity(quantity);
                items.add(item);

                BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                totalPrice = totalPrice.add(itemTotal);
                totalItems += quantity;

                // 记录有效商品
                validProductKeys.add(productId.toString());
            }
        }

        // 如果购物车中有无效商品，重新构建购物车（只保留有效商品）
        if (validProductKeys.size() != cartMap.size()) {
            // 删除整个购物车
            redisTemplate.delete(cartKey);

            // 重新添加有效商品
            if (!validProductKeys.isEmpty()) {
                for (CartItemDTO item : items) {
                    redisTemplate.opsForHash().put(cartKey,
                            item.getProductId().toString(),
                            item.getQuantity());
                }
                // 重新设置过期时间
                redisTemplate.expire(cartKey, CART_EXPIRE_DAYS, TimeUnit.DAYS);
            }
        }

        return new CartResponseDTO(items, totalPrice, totalItems);
    }
    
    // 获取缓存的商品信息，如果缓存不存在则查询数据库并缓存
    private Product getCachedProduct(Long productId) {
        String productCacheKey = PRODUCT_CACHE_PREFIX + productId;
        
        // 尝试从缓存获取
        Product product = (Product) redisTemplate.opsForValue().get(productCacheKey);
        if (product != null) {
            return product;
        }
        
        // 缓存未命中，查询数据库
        product = productMapper.findById(productId);
        if (product != null) {
            // 缓存商品信息
            redisTemplate.opsForValue().set(productCacheKey, product, 
                                          PRODUCT_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        
        return product;
    }
    
    // 获取购物车Redis键
    private String getCartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }
}
