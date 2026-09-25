package com.clothsell.module.mall.vo.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ClientLoginReqVO {
    @NotBlank
    @Pattern(regexp = "1\\d{10}", message = "请填写11位手机号")
    private String phone;
    @NotBlank
    private String password;
}
