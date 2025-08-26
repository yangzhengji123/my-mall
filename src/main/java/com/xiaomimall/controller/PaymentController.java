package com.xiaomimall.controller;

import com.xiaomimall.dto.ApiResponse;
import com.xiaomimall.dto.PaymentConfigDTO;
import com.xiaomimall.dto.PaymentNotifyDTO;
import com.xiaomimall.dto.PaymentRequestDTO;
import com.xiaomimall.dto.PaymentResponseDTO;
import com.xiaomimall.entity.PaymentConfig;
import com.xiaomimall.entity.PaymentType;
import com.xiaomimall.mapper.PaymentConfigMapper;
import com.xiaomimall.service.PaymentService;
import com.xiaomimall.util.SignatureUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付控制器
 * 提供支付功能接口
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    private final SignatureUtil signatureUtil;
    private final PaymentConfigMapper paymentConfigMapper;
    
    // 处理支付通知
    @PostMapping("/notify/{paymentTypeCode}")
    public ResponseEntity<String> handlePaymentNotify(
            @PathVariable Integer paymentTypeCode, // 使用更明确的参数名
            @RequestBody PaymentNotifyDTO notifyDTO,
            @RequestHeader("X-Signature") String signature,
            @RequestHeader("X-Timestamp") String timestamp) { // 时间戳改为必需

        log.info("收到支付通知回调. paymentTypeCode: {}, orderNo: {}", paymentTypeCode, notifyDTO.getOrderNo());
        
        try {
            // 1. 验证时间戳有效性（防止重放攻击）
            if (!isTimestampValid(timestamp)) {
                log.warn("时间戳校验失败. timestamp: {}", timestamp);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
            }
            // 2. 将Integer类型的paymentTypeCode转换为PaymentType枚举
            PaymentType paymentType;
            try {
                paymentType = PaymentType.fromCode(paymentTypeCode);
            } catch (IllegalArgumentException e) {
                log.warn("无效的支付类型码: {}", paymentTypeCode);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
            }

            // 3. 获取支付配置
            PaymentConfig config = paymentConfigMapper.findByPaymentType(paymentType);
            if (config == null || !config.getIsActive()) {
                log.warn("支付配置不存在或未启用. paymentType: {}", paymentType);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("fail");
            }

            // 4. 转换为参数Map用于签名验证
            Map<String, String> paramMap = convertToParamMap(notifyDTO);

            // 5. 验证签名
            if (!signatureUtil.verifySignature(paramMap, config.getPrivateKey(),
                    "HmacSHA256", signature)) {
                log.warn("签名验证失败. orderNo: {}", notifyDTO.getOrderNo());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("fail");
            }

            log.debug("签名验证成功. orderNo: {}", notifyDTO.getOrderNo());
            
            // 6. 处理支付通知
            Boolean result = paymentService.handlePaymentNotify(notifyDTO);
            log.info("支付通知处理结果: {}, orderNo: {}", result, notifyDTO.getOrderNo());
            
            return ResponseEntity.ok(result ? "success" : "fail");

        } catch (Exception e) {
            log.error("处理支付通知回调时发生系统异常. orderNo: {}", notifyDTO.getOrderNo(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> createPayment(
            @RequestBody @Valid PaymentRequestDTO paymentRequest) {
        log.info("创建支付请求. orderId: {}, paymentType: {}", 
                 paymentRequest.getOrderId(), paymentRequest.getPaymentType());
        
        try {
            PaymentResponseDTO response = paymentService.createPayment(paymentRequest);
            if (response.getSuccess()) {
                log.info("支付创建成功. orderNo: {}", response.getOrderNo());
                return ResponseEntity.ok(ApiResponse.success(response));
            } else {
                log.warn("支付创建失败. orderId: {}, message: {}", 
                         paymentRequest.getOrderId(), response.getMessage());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.badRequest(response.getMessage()));
            }
        } catch (Exception e) {
            log.error("创建支付时发生系统异常. orderId: {}", paymentRequest.getOrderId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.internalError("支付创建失败: " + e.getMessage()));
        }
    }

    // 查询支付状态
    @GetMapping("/status/{orderNo}")
    public ResponseEntity<ApiResponse<Boolean>> queryPaymentStatus(
            @PathVariable @Pattern(regexp = "\\d{12,20}", message = "订单号格式错误") String orderNo) {
        log.info("查询支付状态. orderNo: {}", orderNo);
        
        Boolean status = paymentService.queryPaymentStatus(orderNo);
        log.info("支付状态查询结果. orderNo: {}, status: {}", orderNo, status);
        
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    // 获取支付配置（管理员权限）
    @GetMapping("/admin/config/{paymentType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentConfigDTO>> getPaymentConfig(@PathVariable PaymentType paymentType) {
        log.info("获取支付配置. paymentType: {}", paymentType);
        
        PaymentConfigDTO config = paymentService.getPaymentConfig(paymentType);
        
        if (config == null) {
            log.warn("支付配置不存在. paymentType: {}", paymentType);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("支付配置不存在"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    // 更新支付配置（管理员权限）
    @PutMapping("/admin/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentConfigDTO>> updatePaymentConfig(
            @RequestBody PaymentConfigDTO configDTO) {
        log.info("更新支付配置. paymentType: {}", configDTO.getPaymentType());
        
        PaymentConfigDTO updatedConfig = paymentService.updatePaymentConfig(configDTO);
        log.info("支付配置更新成功. paymentType: {}", updatedConfig.getPaymentType());
        
        return ResponseEntity.ok(ApiResponse.success(updatedConfig));
    }
    
    /**
     * 验证时间戳有效性（防止重放攻击）
     */
    private boolean isTimestampValid(String timestamp) {
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            // 允许5分钟的时间差
            final long MAX_TIME_DIFF = 5 * 60 * 1000;
            return Math.abs(currentTime - requestTime) <= MAX_TIME_DIFF;
        } catch (NumberFormatException e) {
            log.warn("时间戳格式错误: {}", timestamp);
            return false;
        }
    }

    /**
     * 将DTO转换为参数Map
     */
    private Map<String, String> convertToParamMap(PaymentNotifyDTO notifyDTO) {
        Map<String, String> paramMap = new HashMap<>();
        if (notifyDTO.getOrderNo() != null) {
            paramMap.put("orderNo", notifyDTO.getOrderNo());
        }
        if (notifyDTO.getPaymentNo() != null) {
            paramMap.put("paymentNo", notifyDTO.getPaymentNo());
        }
        if (notifyDTO.getAmount() != null) {
            paramMap.put("amount", notifyDTO.getAmount().toPlainString());
        }
        if (notifyDTO.getPayTime() != null) {
            paramMap.put("payTime", notifyDTO.getPayTime().toString());
        }
        if (notifyDTO.getStatus() != null) {
            paramMap.put("status", notifyDTO.getStatus().toString());
        }
        // 添加时间戳和随机数到签名参数中
        if (notifyDTO.getTimestamp() != null) {
            paramMap.put("timestamp", notifyDTO.getTimestamp().toString());
        }
        if (notifyDTO.getNonce() != null) {
            paramMap.put("nonce", notifyDTO.getNonce());
        }
        return paramMap;
    }
}