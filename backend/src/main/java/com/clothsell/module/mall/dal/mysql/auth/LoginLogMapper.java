package com.clothsell.module.mall.dal.mysql.auth;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.module.mall.dal.dataobject.auth.LoginLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoginLogMapper extends BaseMapperX<LoginLogDO> {
}
