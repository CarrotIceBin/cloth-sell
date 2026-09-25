package com.clothsell.module.mall.dal.dataobject.auth;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("mall_login_log")
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogDO extends BaseDO {
    @TableId
    private Long id;
    private String userType;
    private Long userId;
    private String account;
    private Integer success;
    private String userIp;
    private String userAgent;
}
