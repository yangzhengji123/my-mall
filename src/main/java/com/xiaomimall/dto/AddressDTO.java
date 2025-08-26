package com.xiaomimall.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * 地址DTO
 * 用于地址的请求和响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long id;

    @NotBlank(message = "收件人姓名不能为空")
    @Length(max = 50, message = "收件人姓名长度不能超过50")
    private String recipient;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式错误")
    private String phone;

    @NotBlank(message = "省份不能为空")
    @Length(max = 20, message = "省份名称过长")
    private String province;

    @NotBlank(message = "城市不能为空")
    @Length(max = 20, message = "城市名称过长")
    private String city;

    @NotBlank(message = "区县不能为空")
    @Length(max = 20, message = "区县名称过长")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Length(max = 200, message = "详细地址过长")
    private String detail;

    @NotNull(message = "是否默认地址不能为空")
    @JsonProperty("isDefault")
    private Boolean isDefault;
}