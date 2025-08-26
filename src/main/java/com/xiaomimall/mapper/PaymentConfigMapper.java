package com.xiaomimall.mapper;

import com.xiaomimall.entity.PaymentConfig;
import com.xiaomimall.entity.PaymentType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.cache.annotation.Cacheable;

/**
 * 支付配置Mapper
 * 管理支付配置数据
 */
@Mapper
public interface PaymentConfigMapper {
    
    // 根据支付类型查询配置
    @Cacheable(value = "paymentConfig", key = "#paymentType")
    @Select("SELECT * FROM payment_configs WHERE payment_type = #{paymentType} AND is_active = true")
    PaymentConfig findByPaymentType(PaymentType paymentType);
    
    // 更新支付配置
    @Update("UPDATE payment_configs SET app_id = #{appId}, merchant_id = #{merchantId}, " +
            "public_key = #{publicKey}, private_key = #{privateKey}, notify_url = #{notifyUrl}, " +
            "is_active = #{isActive} WHERE id = #{id}")
    int update(PaymentConfig config);
}