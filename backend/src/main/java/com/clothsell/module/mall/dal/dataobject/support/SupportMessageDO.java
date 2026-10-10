package com.clothsell.module.mall.dal.dataobject.support;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.clothsell.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mall_support_message")
public class SupportMessageDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long senderId;
    private String senderRole;
    private String senderName;
    private String clientId;
    private String content;
    @TableField(exist = false)
    private String buyerName;
}
