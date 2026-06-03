package com.cesi.web.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置（替换 Springfox）
 *
 * @author cesi
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智碳_能源管理系统_接口文档")
                        .description("采集企业水、电、气、热等能耗数据；帮助企业建立能源管理体系，找到跑冒滴漏，为企业节能提供数据支撑；" +
                                "为企业实现碳跟踪、碳盘查、碳交易、碳汇报的全生命过程；为中国碳达峰-碳中和做出贡献！")
                        .contact(new Contact().name("cesi").url("https://www.cesicloud.com").email("qiuxd@cesi.cn"))
                        .version("2.5.2"))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new Components()
                        .addSecuritySchemes("Authorization", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
