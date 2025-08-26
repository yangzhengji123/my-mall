package com.xiaomimall.service;

import com.xiaomimall.dto.AddressDTO;
import com.xiaomimall.entity.UserAddress;
import com.xiaomimall.exception.NotFoundException;
import com.xiaomimall.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 地址服务实现
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    
    private final AddressMapper addressMapper;
    
    @Override
    @Transactional
    public AddressDTO addAddress(Long userId, AddressDTO addressDTO) {
        // 如果设置为默认地址，清除现有默认地址
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault()) {
            addressMapper.clearDefaultAddress(userId);
        }
        
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setRecipient(addressDTO.getRecipient());
        address.setPhone(addressDTO.getPhone());
        address.setProvince(addressDTO.getProvince());
        address.setCity(addressDTO.getCity());
        address.setDistrict(addressDTO.getDistrict());
        address.setDetail(addressDTO.getDetail());
        address.setIsDefault(addressDTO.getIsDefault());
        
        addressMapper.insert(address);
        return convertToAddressDTO(address);
    }
    
    @Override
    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, AddressDTO addressDTO) {
        UserAddress address = addressMapper.findById(addressId, userId);
        if (address == null) {
            throw new NotFoundException("地址不存在");
        }
        
        // 如果设置为默认地址，清除现有默认地址
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault()) {
            addressMapper.clearDefaultAddress(userId);
        }
        
        address.setRecipient(addressDTO.getRecipient());
        address.setPhone(addressDTO.getPhone());
        address.setProvince(addressDTO.getProvince());
        address.setCity(addressDTO.getCity());
        address.setDistrict(addressDTO.getDistrict());
        address.setDetail(addressDTO.getDetail());
        address.setIsDefault(addressDTO.getIsDefault());
        
        addressMapper.update(address);
        return convertToAddressDTO(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = addressMapper.findById(addressId, userId);
        if (address == null) {
            throw new NotFoundException("地址不存在");
        }

        // 如果删除的是默认地址，设置另一个地址为默认（如果存在）
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.clearDefaultAddress(userId);
            addressMapper.setDefaultAddressToNextAvailable(userId, addressId);
        }

        int deleted = addressMapper.delete(addressId, userId);
        if (deleted == 0) {
            throw new NotFoundException("地址不存在");
        }
    }
    
    @Override
    public List<AddressDTO> getUserAddresses(Long userId) {
        List<UserAddress> addresses = addressMapper.findByUserId(userId);
        return addresses.stream()
                .map(this::convertToAddressDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) {
        UserAddress address = addressMapper.findById(addressId, userId);
        if (address == null) {
            throw new NotFoundException("地址不存在");
        }

        // 使用单一SQL操作确保原子性
        addressMapper.setDefaultAddress(userId, addressId);
    }
    
    private AddressDTO convertToAddressDTO(UserAddress address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setRecipient(address.getRecipient());
        dto.setPhone(address.getPhone());
        dto.setProvince(address.getProvince());
        dto.setCity(address.getCity());
        dto.setDistrict(address.getDistrict());
        dto.setDetail(address.getDetail());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }
}