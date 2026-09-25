package com.clothsell.module.mall.dal.mysql.auth;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.auth.RefreshTokenDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper extends BaseMapperX<RefreshTokenDO> {
    default RefreshTokenDO selectByHash(String tokenHash) {
        return selectOne(new LambdaQueryWrapperX<RefreshTokenDO>().eq(RefreshTokenDO::getTokenHash, tokenHash));
    }
}
