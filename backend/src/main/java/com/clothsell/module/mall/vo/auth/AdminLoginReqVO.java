package com.clothsell.module.mall.vo.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminLoginReqVO {
    @NotBlank
    @Size(max = 64)
    private String username;
    @NotBlank
    @Size(max = 72, message = "密码过长")
    private String password;
}
