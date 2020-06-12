package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Cors跨域原理：请求到达之后，并由此判断是否允许跨域访问
 */
@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        System.out.println("corsFilter.......");
        //1.添加CORS的配置信息
        CorsConfiguration Config = new CorsConfiguration();

        //1) 允许的域，不要写*，否则Cookies就无法使用了
        Config.addAllowedOrigin("http://www.leyou.com");
        Config.addAllowedOrigin("http://manage.leyou.com");

        //2） 是否发送Cookies信息
        Config.setAllowCredentials(true);

        //3) 允许的请求方式
        Config.addAllowedMethod("OPTIONS");
        Config.addAllowedMethod("HEAD");
        Config.addAllowedMethod("GET");
        Config.addAllowedMethod("PUT");
        Config.addAllowedMethod("POST");
        Config.addAllowedMethod("DELETE");
        Config.addAllowedMethod("PATCH");

        //4) 允许的头信息
        Config.addAllowedHeader("*");

        //5） 此次预检有效时长，有效时长内无需预检直接跨域访问
        Config.setMaxAge(3600L);

        //2.添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**",Config);

        //3.返回新的CorsFilter
        return new CorsFilter(configurationSource);
    }
}
