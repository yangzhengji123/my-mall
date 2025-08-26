package com.xiaomimall.service;

import com.xiaomimall.dto.LoginRequest;
import com.xiaomimall.dto.RegisterRequest;
import com.xiaomimall.dto.UpdateUserRequest;
import com.xiaomimall.dto.UserResponse;
import com.xiaomimall.entity.User;
import com.xiaomimall.exception.DuplicateException;
import com.xiaomimall.exception.NotFoundException;
import com.xiaomimall.mapper.UserMapper;
import com.xiaomimall.security.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    // 通过SecurityContextHolder获取当前用户ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((User) authentication.getPrincipal()).getId();
    }
    @Override
    @Transactional
    public UserResponse register(@Valid RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(registerRequest.getUsername()) != null) {
            throw new DuplicateException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (registerRequest.getEmail() != null && 
            userMapper.findByEmail(registerRequest.getEmail()) != null) {
            throw new DuplicateException("邮箱已被注册");
        }
        
        // 检查手机号是否已存在
        if (registerRequest.getPhone() != null && 
            userMapper.findByPhone(registerRequest.getPhone()) != null) {
            throw new DuplicateException("手机号已被注册");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRole(User.UserRole.USER.name());

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        int rows = userMapper.insert(user);
        if (rows != 1) {
            throw new RuntimeException("用户注册失败");
        }
        return convertToResponse(user);
    }

    @Override
    public String login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        return jwtTokenUtil.generateToken(user);
    }

    @Override
    public UserResponse getUserInfo() {
        Long userId = getCurrentUserId();
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        return convertToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserInfo(UpdateUserRequest userUpdate) {
        Long userId = getCurrentUserId();
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new NotFoundException("用户不存在");
        }

        // 更新邮箱（如果提供且不重复）
        if (userUpdate.getEmail() != null && !userUpdate.getEmail().equals(user.getEmail())) {
            if (userMapper.findByEmail(userUpdate.getEmail()) != null) {
                throw new DuplicateException("邮箱已被其他用户占用");
            }
            user.setEmail(userUpdate.getEmail());
        }

        // 更新手机号（如果提供且不重复）
        if (userUpdate.getPhone() != null && !userUpdate.getPhone().equals(user.getPhone())) {
            if (userMapper.findByPhone(userUpdate.getPhone()) != null) {
                throw new DuplicateException("手机号已被其他用户占用");
            }
            user.setPhone(userUpdate.getPhone());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateUserInfo(user);
        return convertToResponse(user);
    }
    
    private UserResponse convertToResponse(@NonNull User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
