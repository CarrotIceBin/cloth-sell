package com.clothsell.module.mall.dal.mysql.review;

import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.review.ReviewDO;
import com.clothsell.module.mall.vo.review.ReviewPageReqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReviewMapper extends BaseMapperX<ReviewDO> {
    default PageResult<ReviewDO> selectPage(ReviewPageReqVO reqVO) {
        LambdaQueryWrapperX<ReviewDO> query = new LambdaQueryWrapperX<ReviewDO>()
                .eqIfPresent(ReviewDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ReviewDO::getRating, reqVO.getRating())
                .eqIfPresent(ReviewDO::getPublished, reqVO.getPublished());
        if (reqVO.getOrderBySql() != null) {
            query.last("ORDER BY " + reqVO.getOrderBySql());
        } else {
            query.orderByDesc(ReviewDO::getId);
        }
        return selectPage(reqVO, query);
    }

    default List<ReviewDO> selectByProductId(Long productId) {
        return selectList(new LambdaQueryWrapperX<ReviewDO>()
                .eq(ReviewDO::getProductId, productId)
                .eq(ReviewDO::getPublished, true));
    }

    default boolean existsByProductAndUser(Long productId, Long userId) {
        return selectCount(new LambdaQueryWrapperX<ReviewDO>()
                .eq(ReviewDO::getProductId, productId)
                .eq(ReviewDO::getUserId, userId)) > 0;
    }
}
