package com.xiaomimall.security;

import com.xiaomimall.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")// 从配置文件读取JWT密钥
    private String secret;

    @Value("${jwt.expiration}")// 从配置文件读取JWT过期时间
    private Long expiration;

    public String generateToken(@NonNull User user) {
        // 构建自定义claims（JWT的负载，存储额外用户信息)
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());// 存储用户ID
        claims.put("username", user.getUsername());// 存储用户名
        claims.put("role", user.getRole());//存储用户角色（用于授权）
        // 调用createToken生成令牌，subject通常用用户名
        return createToken(claims, user.getUsername());
    }
//令牌生成方法
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()// 使用jjwt的builder构建JWT
                .setClaims(claims)// 设置自定义负载（claims）
                .setSubject(subject)// 设置标准字段：主题（通常为用户名）
                .setIssuedAt(new Date(System.currentTimeMillis()))// 签发时间（当前时间）
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间（当前时间+有效期）
                .signWith(getSignKey(), SignatureAlgorithm.HS256)// 设置签名密钥和算法（HS256）
                .compact();// 压缩为最终的JWT字符串
    }
//获取签名密钥：getSignKey()
    private Key getSignKey() {
        // 将Base64编码的secret解码为字节数组
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        // 生成HS256算法所需的密钥对象
        return Keys.hmacShaKeyFor(keyBytes);
    }


//解析 JWT 中的信息（提取 claims）
    //提取用户名
    public String extractUsername(String token) {
        // 调用通用方法extractClaim，通过Claims::getSubject提取主题（用户名）
        return extractClaim(token, Claims::getSubject);
    }
//提取用户 ID
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token); // 获取所有claims
        return claims.get("id", Long.class);// 提取自定义的"id"字段
    }

//提取过期时间
    public Date extractExpiration(String token) {
        // 提取标准字段"expiration"
        return extractClaim(token, Claims::getExpiration);
    }
//通用提取方法
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);// 获取所有claims
        return claimsResolver.apply(claims); // 用Function提取指定字段
    }
//获取所有 claims
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder() // 创建解析器构建器
                .setSigningKey(getSignKey())// 设置签名密钥（用于验证令牌合法性）
                .build() // 构建解析器
                .parseClaimsJws(token) // 解析令牌（若签名验证失败，会抛出异常）
                .getBody();// 获取负载（claims）
    }
    // 检查令牌是否过期
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());// 过期时间在当前时间之前 → 已过期
    }
    // 验证令牌有效性：validateToken(String token, UserDetails userDetails)
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);// 从令牌提取用户名
        // 验证：用户名匹配（令牌中的用户名 == 系统中的用户名）+ 令牌未过期
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}