package com.clothsell.module.mall.dal.mysql.product;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.product.SkuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SkuMapper extends BaseMapperX<SkuDO> {
    default List<SkuDO> selectByProductIds(Collection<Long> productIds) {
        return selectList(new LambdaQueryWrapperX<SkuDO>().inIfPresent(SkuDO::getProductId, productIds));
    }

    @Select("SELECT * FROM mall_sku WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    SkuDO selectForUpdate(Long id);
}
