package com.xiaomimall.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;
}
