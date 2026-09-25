package com.clothsell.module.mall.dal.mysql.product;

import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.product.ProductDO;
import com.clothsell.module.mall.vo.product.ProductPageReqVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapperX<ProductDO> {
    default PageResult<ProductDO> selectPage(ProductPageReqVO reqVO) {
        LambdaQueryWrapperX<ProductDO> query = new LambdaQueryWrapperX<ProductDO>()
                .likeIfPresent(ProductDO::getName, reqVO.getName())
                .eqIfPresent(ProductDO::getOnShelf, reqVO.getOnShelf())
                .betweenIfPresent(ProductDO::getCreateTime, reqVO.getCreateTime());
        if (reqVO.getOrderBySql() != null) {
            query.last("ORDER BY " + reqVO.getOrderBySql());
        } else {
            query.orderByDesc(ProductDO::getId);
        }
        return selectPage(reqVO, query);
    }
}
