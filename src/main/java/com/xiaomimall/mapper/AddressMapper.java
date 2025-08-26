package com.xiaomimall.mapper;

import com.xiaomimall.entity.UserAddress;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 收货地址Mapper
 * 管理用户的收货地址
 */
@Mapper
public interface AddressMapper {
    
    // 插入新地址
    @Insert("INSERT INTO user_addresses (user_id, recipient, phone, province, city, district, detail, is_default) " +
            "VALUES (#{userId}, #{recipient}, #{phone}, #{province}, #{city}, #{district}, #{detail}, #{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAddress address);
    
    // 更新地址
    @Update("UPDATE user_addresses SET recipient = #{recipient}, phone = #{phone}, province = #{province}, " +
            "city = #{city}, district = #{district}, detail = #{detail}, is_default = #{isDefault} " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int update(UserAddress address);
    
    // 删除地址
    @Delete("DELETE FROM user_addresses WHERE id = #{id} AND user_id = #{userId}")
    int delete(Long id, Long userId);
    
    // 根据ID查询地址
    @Select("SELECT * FROM user_addresses WHERE id = #{id} AND user_id = #{userId}")
    UserAddress findById(Long id, Long userId);
    
    // 查询用户的所有地址
    @Select("SELECT * FROM user_addresses WHERE user_id = #{userId} ORDER BY is_default DESC, id ASC")
    List<UserAddress> findByUserId(Long userId);
    
    // 清除用户的默认地址
    @Update("UPDATE user_addresses SET is_default = false WHERE user_id = #{userId}")
    int clearDefaultAddress(Long userId);

    @Update("UPDATE user_addresses SET is_default = CASE " +
            "WHEN id = #{addressId} THEN true " +
            "ELSE false END " +
            "WHERE user_id = #{userId}")
    int setDefaultAddress(@Param("userId") Long userId, @Param("addressId") Long addressId);

    @Update("UPDATE user_addresses SET is_default = true " +
            "WHERE user_id = #{userId} AND id != #{excludeId} " +
            "ORDER BY id LIMIT 1")
    int setDefaultAddressToNextAvailable(@Param("userId") Long userId, @Param("excludeId") Long excludeId);

}