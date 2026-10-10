package com.clothsell.module.mall.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mall_chat_message")
public class ChatMessageDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String senderRole;
    private Long senderId;
    private String senderName;
    private String clientId;
    private String content;
}
