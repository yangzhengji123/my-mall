package com.xiaomimall.controller;

import com.xiaomimall.dto.ApiResponse;
import com.xiaomimall.dto.OrderCreateDTO;
import com.xiaomimall.dto.OrderDTO;
import com.xiaomimall.entity.User;
import com.xiaomimall.security.CurrentUser;
import com.xiaomimall.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @CurrentUser User user,
            @Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        OrderDTO order = orderService.createOrder(user.getId(), orderCreateDTO);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderDetail(
            @CurrentUser User user,
            @PathVariable Long orderId) {
        OrderDTO order = orderService.getOrderDetail(user.getId(), orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getUserOrders(@CurrentUser User user) {
        List<OrderDTO> orders = orderService.getUserOrders(user.getId());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @CurrentUser User user,
            @PathVariable Long orderId) {
        orderService.cancelOrder(user.getId(), orderId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<ApiResponse<Void>> payOrder(
            @CurrentUser User user,
            @PathVariable Long orderId) {
        orderService.payOrder(user.getId(), orderId);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponse<Void>> deliverOrder(
            @PathVariable Long orderId,
            @RequestParam String trackingNumber) {
        orderService.deliverOrder(orderId, trackingNumber);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeOrder(
            @CurrentUser User user,
            @PathVariable Long orderId) {
        orderService.completeOrder(user.getId(), orderId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}