package com.clothsell.module.mall.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mall_chat_room")
public class ChatRoomDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String title;
    private String ownerRole;
    private Long ownerId;
    private String directKey;
    private Long lastMessageId;
    private String lastContent;
}
