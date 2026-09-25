package com.clothsell.module.mall.vo.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OrderSaveReqVO {
    private Long userId;
    @NotBlank(message = "请填写收货人")
    private String receiverName;
    @NotBlank(message = "请填写收货手机")
    @Pattern(regexp = "1\\d{10}", message = "请填写收货手机")
    private String receiverPhone;
    @NotBlank(message = "请填写省")
    private String province;
    @NotBlank(message = "请填写市")
    private String city;
    @NotBlank(message = "请填写区")
    private String district;
    private String region;
    @NotBlank(message = "请填写详细地址")
    private String address;
}
