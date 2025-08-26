package com.xiaomimall.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口 - 无需认证即可访问
                .requestMatchers("/api/auth/**").permitAll()           // 认证相关接口
                .requestMatchers("/api/products/{id}").permitAll()     // 商品详情
                .requestMatchers("/api/products/search").permitAll()   // 商品搜索
                .requestMatchers("/api/categories").permitAll()        // 分类列表
                .requestMatchers("/api/banners/active").permitAll()    // 轮播图列表
                .requestMatchers("/api/seckill/active").permitAll()    // 秒杀活动列表
                .requestMatchers("/api/seckill/upcoming").permitAll()  // 即将开始的秒杀
                .requestMatchers("/orders/notify").permitAll()         // 支付回调通知
                
                // 用户相关接口 - 需要 USER 或 ADMIN 角色
                .requestMatchers("/api/users/me").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/cart/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/orders/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/addresses/**").hasAnyRole("USER", "ADMIN")
                
                // 管理员接口 - 仅 ADMIN 角色可访问
                .requestMatchers("/api/products/**").hasRole("ADMIN")
                .requestMatchers("/api/categories/**").hasRole("ADMIN")
                .requestMatchers("/api/banners/**").hasRole("ADMIN")
                .requestMatchers("/api/seckill/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/payment/admin/**").hasRole("ADMIN")
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
