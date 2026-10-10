package com.clothsell.module.mall.service.auth;

import com.clothsell.module.mall.vo.auth.AdminLoginReqVO;
import com.clothsell.module.mall.vo.auth.ClientLoginReqVO;
import com.clothsell.module.mall.vo.auth.LoginRespVO;
import com.clothsell.module.mall.vo.auth.RefreshReqVO;

public interface AuthService {
    LoginRespVO register(ClientLoginReqVO reqVO);

    LoginRespVO login(ClientLoginReqVO reqVO);

    LoginRespVO adminLogin(AdminLoginReqVO reqVO);

    LoginRespVO refresh(RefreshReqVO reqVO);

    void logout(RefreshReqVO reqVO);
}
