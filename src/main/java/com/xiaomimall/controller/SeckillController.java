package com.xiaomimall.controller;

import com.xiaomimall.dto.ApiResponse;
import com.xiaomimall.dto.SeckillActivityDTO;
import com.xiaomimall.dto.SeckillResultDTO;
import com.xiaomimall.entity.PaymentType;
import com.xiaomimall.entity.User;
import com.xiaomimall.security.CurrentUser;
import com.xiaomimall.service.SeckillService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {
    
    private final SeckillService seckillService;

    // 执行秒杀
    @PostMapping("/{seckillId}")
    public ResponseEntity<ApiResponse<SeckillResultDTO>> executeSeckill(
            @CurrentUser @NonNull User user,
            @PathVariable @NotNull(message = "秒杀ID不能为空") Long seckillId,
            @NotNull  @RequestParam("payment") PaymentType paymentType) {
        SeckillResultDTO result = seckillService.executeSeckill(user.getId(), seckillId, paymentType);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 获取当前有效的秒杀活动
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<SeckillActivityDTO>>> getActiveSeckills() {
        List<SeckillActivityDTO> activities = seckillService.getActiveSeckills();
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    // 获取即将开始的秒杀活动
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<SeckillActivityDTO>>> getUpcomingSeckills() {
        List<SeckillActivityDTO> activities = seckillService.getUpcomingSeckills();
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    // 创建秒杀活动（管理员权限）
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/activities")
    public ResponseEntity<ApiResponse<SeckillActivityDTO>> createSeckillActivity(
            @RequestBody @Valid SeckillActivityDTO activityDTO) {
        SeckillActivityDTO result = seckillService.createSeckillActivity(activityDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 更新秒杀活动（管理员权限）
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/activities/{id}")
    public ResponseEntity<ApiResponse<SeckillActivityDTO>> updateSeckillActivity(
            @PathVariable Long id,
            @RequestBody @Valid SeckillActivityDTO activityDTO) {
        SeckillActivityDTO result = seckillService.updateSeckillActivity(id, activityDTO);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 删除秒杀活动（管理员权限）
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/activities/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSeckillActivity(@PathVariable Long id) {
        seckillService.deleteSeckillActivity(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
