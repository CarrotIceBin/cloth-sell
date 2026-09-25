package com.clothsell.module.mall.vo.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderSaveReqVO {
    private Long userId;
    @NotBlank(message = "请填写收货人")
    @Size(max = 32, message = "收货人过长")
    private String receiverName;
    @NotBlank(message = "请填写收货手机")
    @Pattern(regexp = "1\\d{10}", message = "请填写收货手机")
    private String receiverPhone;
    @NotBlank(message = "请填写省")
    @Size(max = 32, message = "省过长")
    private String province;
    @NotBlank(message = "请填写市")
    @Size(max = 32, message = "市过长")
    private String city;
    @NotBlank(message = "请填写区")
    @Size(max = 32, message = "区过长")
    private String district;
    private String region;
    @NotBlank(message = "请填写详细地址")
    @Size(max = 200, message = "详细地址过长")
    private String address;
}
