package com.clothsell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ClothSellApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ClothSellApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String url = env.getProperty("spring.datasource.url", "");
        String db = url.startsWith("jdbc:postgresql") ? "PostgreSQL" : "MySQL";
        boolean redis = Boolean.parseBoolean(env.getProperty("app.cache.redis", "true"));
        System.out.println("启动 Tomcat，端口 " + port);
        System.out.println("连接 " + db);
        System.out.println("装载 MyBatis、登录校验和接口前缀");
        System.out.println(redis ? "商品缓存：Redis" : "商品缓存：关闭");
        System.out.println("金额读写：" + env.getProperty("app.money-url", "http://127.0.0.1:5088"));
        System.out.println("检查管理员账号");
        System.out.println("===========项目启动成功");
    }
}
