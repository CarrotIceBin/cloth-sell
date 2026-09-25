package com.clothsell.module.mall.dal.mysql.cart;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.cart.CartDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapperX<CartDO> {
    default List<CartDO> selectByUserId(Long userId) {
        return selectList(new LambdaQueryWrapperX<CartDO>().eq(CartDO::getUserId, userId).orderByAsc(CartDO::getId));
    }

    default CartDO selectByUserAndSku(Long userId, Long skuId) {
        return selectOne(new LambdaQueryWrapperX<CartDO>().eq(CartDO::getUserId, userId).eq(CartDO::getSkuId, skuId));
    }
}
