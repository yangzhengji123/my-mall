package com.xiaomimall.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

// 用户信息响应DTO
@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}