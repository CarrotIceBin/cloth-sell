package com.clothsell.module.mall.dal.dataobject.auth;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("mall_admin")
@Data
@EqualsAndHashCode(callSuper = true)
public class MallAdminDO extends BaseDO {
    @TableId
    private Long id;
    private String username;
    private String password;
}
