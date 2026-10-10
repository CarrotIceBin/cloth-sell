package com.clothsell.module.mall.dal.dataobject.chat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mall_chat_member")
public class ChatMemberDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String role;
    private Long accountId;
    private String name;
}
