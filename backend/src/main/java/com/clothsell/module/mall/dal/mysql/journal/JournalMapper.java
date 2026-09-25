package com.clothsell.module.mall.dal.mysql.journal;

import com.clothsell.framework.common.pojo.PageResult;
import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.clothsell.module.mall.dal.dataobject.journal.JournalDO;
import com.clothsell.module.mall.vo.journal.JournalPageReqVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JournalMapper extends BaseMapperX<JournalDO> {
    default PageResult<JournalDO> selectPage(JournalPageReqVO reqVO) {
        LambdaQueryWrapperX<JournalDO> query = new LambdaQueryWrapperX<JournalDO>()
                .likeIfPresent(JournalDO::getTitle, reqVO.getTitle())
                .eqIfPresent(JournalDO::getTag, reqVO.getTag())
                .eqIfPresent(JournalDO::getPublished, reqVO.getPublished());
        if (reqVO.getOrderBySql() != null) {
            query.last("ORDER BY " + reqVO.getOrderBySql());
        } else {
            query.orderByDesc(JournalDO::getId);
        }
        return selectPage(reqVO, query);
    }
}
