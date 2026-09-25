package com.clothsell.module.mall.dal.mysql.order;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.order.OrderLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface OrderLineMapper extends BaseMapperX<OrderLineDO> {
    default List<OrderLineDO> selectByOrderIds(Collection<Long> orderIds) {
        return selectList(new LambdaQueryWrapperX<OrderLineDO>().inIfPresent(OrderLineDO::getOrderId, orderIds));
    }

    default boolean existsBySkuId(Long skuId) {
        return selectCount(new LambdaQueryWrapperX<OrderLineDO>().eq(OrderLineDO::getSkuId, skuId)) > 0;
    }
}
