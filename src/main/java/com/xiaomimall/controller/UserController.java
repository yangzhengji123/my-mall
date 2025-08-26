package com.xiaomimall.controller;

import com.xiaomimall.dto.ApiResponse;
import com.xiaomimall.dto.UpdateUserRequest;
import com.xiaomimall.dto.UserResponse;
import com.xiaomimall.entity.User;
import com.xiaomimall.security.CurrentUser;
import com.xiaomimall.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@CurrentUser User currentUser) {
        // 修改点：移除多余的userId参数传递
        UserResponse userResponse = userService.getUserInfo();  // 不需要传参
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @CurrentUser User currentUser,
            @RequestBody @Valid UpdateUserRequest userUpdate) {
        // 修改点：移除userId参数传递
        UserResponse updatedUser = userService.updateUserInfo(userUpdate);  // 仅传递更新参数
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }
}
