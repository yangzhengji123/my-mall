package com.xiaomimall.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
@Slf4j
public class SignatureUtil {
    
    /**
     * 生成签名
     * @param params 参数Map
     * @param secret 密钥
     * @param algorithm 算法，如HmacSHA256
     * @return 签名
     */
    public static String generateSignature(Map<String, String> params, String secret, String algorithm) {
        try {
            // 1. 参数排序
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);
            
            // 2. 拼接参数字符串
            StringBuilder stringToSign = new StringBuilder();
            for (String key : keys) {
                if ("sign".equals(key) || "signature".equals(key)) {
                    continue; // 跳过签名参数本身
                }
                String value = params.get(key);
                if (value != null && !value.trim().isEmpty()) {
                    stringToSign.append(key).append("=").append(value).append("&");
                }
            }
            
            // 移除最后一个&
            if (stringToSign.length() > 0) {
                stringToSign.setLength(stringToSign.length() - 1);
            }
            
            // 3. 使用HMAC算法生成签名
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm);
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(stringToSign.toString().getBytes(StandardCharsets.UTF_8));
            
            // 4. 转换为16进制字符串
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("签名生成失败", e);
        }
    }
    
    /**
     * 验证签名
     */
    public static boolean verifySignature(Map<String, String> params, String secret, 
                                        String algorithm, String receivedSignature) {
        if (receivedSignature == null || receivedSignature.isEmpty()) {
            return false;
        }
        
        String generatedSignature = generateSignature(params, secret, algorithm);
        return generatedSignature.equalsIgnoreCase(receivedSignature);
    }
    
    /**
     * 字节数组转16进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}