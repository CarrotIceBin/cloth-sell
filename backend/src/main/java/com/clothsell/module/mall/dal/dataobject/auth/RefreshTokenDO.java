package com.clothsell.module.mall.dal.dataobject.auth;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@TableName("mall_refresh_token")
@Data
@EqualsAndHashCode(callSuper = true)
public class RefreshTokenDO extends BaseDO {
    @TableId
    private Long id;
    private String userType;
    private Long userId;
    private String account;
    private String tokenHash;
    private LocalDateTime expireTime;
}
