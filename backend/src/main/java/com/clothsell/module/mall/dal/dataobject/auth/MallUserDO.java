package com.clothsell.module.mall.dal.dataobject.auth;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("mall_user")
@Data
@EqualsAndHashCode(callSuper = true)
public class MallUserDO extends BaseDO {
    @TableId
    private Long id;
    private String phone;
    private String password;
}
