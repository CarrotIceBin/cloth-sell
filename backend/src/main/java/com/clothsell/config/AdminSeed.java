package com.clothsell.config;

import com.clothsell.module.mall.dal.dataobject.auth.MallAdminDO;
import com.clothsell.module.mall.dal.mysql.auth.MallAdminMapper;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeed implements CommandLineRunner {
    @Resource
    private MallAdminMapper mallAdminMapper;

    @Override
    public void run(String... args) {
        if (mallAdminMapper.selectByUsername("admin") != null) {
            return;
        }
        MallAdminDO admin = new MallAdminDO();
        admin.setUsername("admin");
        admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
        mallAdminMapper.insert(admin);
    }
}
