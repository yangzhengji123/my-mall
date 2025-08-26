package com.xiaomimall.controller;

import com.xiaomimall.dto.AddressDTO;
import com.xiaomimall.dto.ApiResponse;
import com.xiaomimall.entity.User;
import com.xiaomimall.security.CurrentUser;
import com.xiaomimall.service.AddressService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址控制器
 * 提供收货地址管理接口
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;

    // 添加收货地址
    @PostMapping
    public ResponseEntity<ApiResponse<AddressDTO>> addAddress(
            @CurrentUser @NonNull User user,
            @RequestBody @Valid AddressDTO addressDTO) {
        AddressDTO result = addressService.addAddress(user.getId(), addressDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 更新收货地址
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @CurrentUser @NonNull User user,
            @PathVariable Long addressId,
            @RequestBody @Valid AddressDTO addressDTO) {
        AddressDTO result = addressService.updateAddress(user.getId(), addressId, addressDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 删除收货地址
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @CurrentUser @NonNull User user,
            @PathVariable Long addressId) {
        addressService.deleteAddress(user.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 获取用户所有收货地址
    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getUserAddresses(@CurrentUser @NonNull User user) {
        List<AddressDTO> addresses = addressService.getUserAddresses(user.getId());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    // 设置默认地址
    @PostMapping("/{addressId}/set-default")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(
            @CurrentUser @NonNull User user,
            @PathVariable Long addressId) {
        addressService.setDefaultAddress(user.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
