package com.bolsadeideas.springboot.backend.apirest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.TimeZone;

@SpringBootApplication
public class SpringBootBackendApirestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootBackendApirestApplication.class, args);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
    }
}
