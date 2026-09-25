package com.clothsell.module.mall.vo.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshReqVO {
    @NotBlank
    private String refreshToken;
}
