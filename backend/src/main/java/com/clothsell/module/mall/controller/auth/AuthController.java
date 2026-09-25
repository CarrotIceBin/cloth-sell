package com.clothsell.module.mall.controller.auth;

import com.clothsell.framework.common.pojo.CommonResult;
import com.clothsell.module.mall.service.auth.AuthService;
import com.clothsell.module.mall.vo.auth.AdminLoginReqVO;
import com.clothsell.module.mall.vo.auth.ClientLoginReqVO;
import com.clothsell.module.mall.vo.auth.LoginRespVO;
import com.clothsell.module.mall.vo.auth.RefreshReqVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.clothsell.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/mall/auth")
public class AuthController {
    @Resource
    private AuthService authService;

    @PostMapping("/register")
    public CommonResult<LoginRespVO> register(@Valid @RequestBody ClientLoginReqVO reqVO) {
        return success(authService.register(reqVO));
    }

    @PostMapping("/login")
    public CommonResult<LoginRespVO> login(@Valid @RequestBody ClientLoginReqVO reqVO) {
        return success(authService.login(reqVO));
    }

    @PostMapping("/admin-login")
    public CommonResult<LoginRespVO> adminLogin(@Valid @RequestBody AdminLoginReqVO reqVO) {
        return success(authService.adminLogin(reqVO));
    }

    @PostMapping("/refresh")
    public CommonResult<LoginRespVO> refresh(@Valid @RequestBody RefreshReqVO reqVO) {
        return success(authService.refresh(reqVO));
    }

    @PostMapping("/logout")
    public CommonResult<Boolean> logout(@RequestBody RefreshReqVO reqVO) {
        authService.logout(reqVO);
        return success(true);
    }
}
