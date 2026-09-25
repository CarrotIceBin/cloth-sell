package com.clothsell.module.mall.vo.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientLoginReqVO {
    @NotBlank
    @Pattern(regexp = "1\\d{10}", message = "请填写11位手机号")
    private String phone;
    @NotBlank
    @Size(max = 72, message = "密码过长")
    private String password;
}
