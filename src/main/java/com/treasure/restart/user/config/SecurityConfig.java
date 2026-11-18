
package com.treasure.restart.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API 不使用 CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 关闭默认表单登录
                .formLogin(AbstractHttpConfigurer::disable)

                // 关闭 HTTP Basic 认证
                .httpBasic(AbstractHttpConfigurer::disable)

                // 关闭默认登出
                .logout(AbstractHttpConfigurer::disable)

                // Spring Security 不负责接口权限判断
                // JWT 由自定义 JwtInterceptor 校验
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
