package com.clothsell.module.mall.vo.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginReqVO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
