package com.clothsell.config;

import com.clothsell.module.mall.dal.dataobject.auth.MallAdminDO;
import com.clothsell.module.mall.dal.mysql.auth.MallAdminMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminSeed implements CommandLineRunner {
    @Resource
    private MallAdminMapper mallAdminMapper;
    @Value("${app.admin-password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (mallAdminMapper.selectByUsername("admin") != null) {
            return;
        }
        if (adminPassword == null || adminPassword.length() < 8) {
            log.warn("mall_admin 没有 admin。设置至少 8 位的 app.admin-password 后才会创建管理员");
            return;
        }
        MallAdminDO admin = new MallAdminDO();
        admin.setUsername("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode(adminPassword));
        mallAdminMapper.insert(admin);
    }
}
