package com.xiaomimall.service;

import com.xiaomimall.dto.LoginRequest;
import com.xiaomimall.dto.RegisterRequest;
import com.xiaomimall.dto.UpdateUserRequest;
import com.xiaomimall.dto.UserResponse;
import jakarta.validation.Valid;

public interface UserService {
    UserResponse register(@Valid RegisterRequest registerRequest);
    String login(LoginRequest loginRequest);
    UserResponse getUserInfo();
    UserResponse updateUserInfo(UpdateUserRequest userUpdate);
}
