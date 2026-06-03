package com.cesi.framework.config;

import java.util.TimeZone;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * 程序注解配置
 *
 * @author cesi
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.cesi.**.mapper")
public class ApplicationConfig
{
    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        return Jackson2ObjectMapperBuilder.json()
                .timeZone(TimeZone.getDefault())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(module)
                .build();
    }
}
