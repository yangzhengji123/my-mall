package com.xiaomimall.service;

import com.xiaomimall.dto.AddressDTO;

import java.util.List;

/**
 * 地址服务接口
 * 管理用户收货地址
 */
public interface AddressService {
    AddressDTO addAddress(Long userId, AddressDTO addressDTO);
    AddressDTO updateAddress(Long userId, Long addressId, AddressDTO addressDTO);
    void deleteAddress(Long userId, Long addressId);
    List<AddressDTO> getUserAddresses(Long userId);
    void setDefaultAddress(Long userId, Long addressId);
}