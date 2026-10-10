package com.clothsell.module.mall.dal.mysql.order;

import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.order.OrderDO;
import com.clothsell.module.mall.vo.order.OrderPageReqVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapperX<OrderDO> {
    @Select("SELECT * FROM mall_order WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    OrderDO selectForUpdate(Long id);

    default PageResult<OrderDO> selectPage(OrderPageReqVO reqVO) {
        LambdaQueryWrapperX<OrderDO> query = new LambdaQueryWrapperX<OrderDO>()
                .eqIfPresent(OrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OrderDO::getUserId, reqVO.getUserId())
                .betweenIfPresent(OrderDO::getCreateTime, reqVO.getCreateTime());
        if (reqVO.getOrderBySql() != null) {
            query.last("ORDER BY " + reqVO.getOrderBySql());
        } else {
            query.orderByDesc(OrderDO::getId);
        }
        return selectPage(reqVO, query);
    }
}
