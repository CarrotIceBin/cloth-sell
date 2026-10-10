package com.clothsell.module.mall.dal.mysql.chat;

import com.clothsell.framework.mybatis.core.mapper.BaseMapperX;
import com.clothsell.module.mall.dal.dataobject.chat.ChatMemberDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMemberMapper extends BaseMapperX<ChatMemberDO> { }
