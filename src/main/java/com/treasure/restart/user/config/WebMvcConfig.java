package com.treasure.restart.user.config;

import com.treasure.restart.user.helper.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    /**
     * JWT 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/loginPwd",
                        "/api/user/register"
                );
    }

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://127.0.0.1:5500",
                        "http://localhost:5500",
                        "http://127.0.0.1:5173",
                        "http://localhost:5173",
                        "https://www.treasurect.site",
                        "https://treasurect.site"
                )
                .allowedMethods(
                        "GET", "POST", "PUT", "DELETE", "OPTIONS"
                )
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
