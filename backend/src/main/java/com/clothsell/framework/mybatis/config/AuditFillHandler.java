package com.clothsell.framework.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuditFillHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now().withNano(0);
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "creator", String.class, SecurityFrameworkUtils.getUserName());
        strictInsertFill(metaObject, "updater", String.class, SecurityFrameworkUtils.getUserName());
        strictInsertFill(metaObject, "deleted", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now().withNano(0));
        strictUpdateFill(metaObject, "updater", String.class, SecurityFrameworkUtils.getUserName());
    }
}
