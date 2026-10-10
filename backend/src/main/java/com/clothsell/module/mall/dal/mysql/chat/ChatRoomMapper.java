package com.clothsell.module.mall.dal.mysql.chat;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.module.mall.dal.dataobject.chat.ChatRoomDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatRoomMapper extends BaseMapperX<ChatRoomDO> {
    @org.apache.ibatis.annotations.Select("""
            SELECT r.* FROM mall_chat_room r JOIN mall_chat_member m ON m.room_id = r.id
            WHERE m.role = #{role} AND m.account_id = #{accountId} AND m.deleted = 0 AND r.deleted = 0
            ORDER BY r.last_message_id DESC, r.id DESC LIMIT 50 OFFSET #{offset}
            """)
    java.util.List<ChatRoomDO> rooms(@org.apache.ibatis.annotations.Param("role") String role,
            @org.apache.ibatis.annotations.Param("accountId") Long accountId,
            @org.apache.ibatis.annotations.Param("offset") int offset);
}
