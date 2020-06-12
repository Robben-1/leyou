package com.leyou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件上传配置类
 * 读取并注入application.yml中以leyou.upload为前缀的配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "leyou.upload")
public class UpLoadProperties {

    private String baseUrl;
    private List<String> allowType;
}
