package com.clothsell.module.mall.dal.mysql.auth;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.auth.MallUserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MallUserMapper extends BaseMapperX<MallUserDO> {
    default MallUserDO selectByPhone(String phone) {
        return selectOne(new LambdaQueryWrapperX<MallUserDO>().eq(MallUserDO::getPhone, phone));
    }
}
