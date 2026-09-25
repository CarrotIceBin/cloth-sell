package com.clothsell.framework.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/files/**").addResourceLocations(location);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "http://192.168.*:*",
                        "http://10.*:*",
                        "https://*.trycloudflare.com",
                        "https://*.mylyx.store"
                )
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
