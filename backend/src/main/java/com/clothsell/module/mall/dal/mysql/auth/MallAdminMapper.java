package com.clothsell.module.mall.dal.mysql.auth;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.auth.MallAdminDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MallAdminMapper extends BaseMapperX<MallAdminDO> {
    default MallAdminDO selectByUsername(String username) {
        return selectOne(new LambdaQueryWrapperX<MallAdminDO>().eq(MallAdminDO::getUsername, username));
    }
}
