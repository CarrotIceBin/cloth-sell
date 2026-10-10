package com.clothsell.module.mall.dal.mysql.support;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.module.mall.dal.dataobject.support.SupportMessageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SupportMessageMapper extends BaseMapperX<SupportMessageDO> {
    @Select("""
            SELECT m.*, (
              SELECT b.sender_name FROM mall_support_message b
              WHERE b.user_id = m.user_id AND b.sender_role = 'USER' AND b.deleted = 0
              ORDER BY b.id DESC LIMIT 1
            ) AS buyer_name FROM mall_support_message m
            WHERE m.deleted = 0 AND m.id = (
              SELECT MAX(s.id) FROM mall_support_message s WHERE s.user_id = m.user_id AND s.deleted = 0
            ) ORDER BY m.id DESC LIMIT 50 OFFSET #{offset}
            """)
    List<SupportMessageDO> conversations(@Param("offset") int offset);
}
