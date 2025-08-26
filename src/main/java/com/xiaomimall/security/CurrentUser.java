package com.xiaomimall.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.lang.annotation.*;
//自定义 @CurrentUser 注解
@Target({ElementType.PARAMETER, ElementType.TYPE})//规定 @CurrentUser 可以标注的位置
@Retention(RetentionPolicy.RUNTIME)//规定注解的生命周期,RetentionPolicy.RUNTIME 表示注解会保留到运行时
@Documented
@AuthenticationPrincipal//用于从安全上下文中获取当前认证用户的信息（如用户对象、用户名等）
public @interface CurrentUser {
}