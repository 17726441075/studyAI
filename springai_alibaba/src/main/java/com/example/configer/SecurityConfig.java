package com.example.configer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置类
 * 配置用户认证和权限校验规则
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * 密码加密器
     * 使用BCrypt算法对密码进行加密存储
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用户详情服务
     * 配置两个用户：
     * - user/123456：普通用户角色（USER）
     * - admin/admin123：管理员角色（ADMIN）
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // 普通用户 - 仅有USER角色
        UserDetails normalUser = User.withUsername("user")
                .password(passwordEncoder.encode("123456"))
                .roles("USER")
                .build();

        // 管理员用户 - 有ADMIN角色
        UserDetails adminUser = User.withUsername("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(normalUser, adminUser);
    }

    /**
     * 安全过滤链配置
     *
     * 配置说明：
     * - 所有请求都需要认证
     * - 使用HTTP Basic认证（简化演示）
     * - 禁用CSRF（开发环境使用，生产环境需配置）
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 所有请求都需要身份认证
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // 启用HTTP Basic认证
                .httpBasic(Customizer.withDefaults())
                // 禁用CSRF（开发演示用，生产环境必须启用）
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}