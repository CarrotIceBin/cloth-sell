package com.clothsell.module.mall.vo.auth;

import lombok.Data;

import java.util.List;

@Data
public class LoginRespVO {
    private String accessToken;
    private String refreshToken;
    private String token;
    private String name;
    private List<String> permissions;
}
