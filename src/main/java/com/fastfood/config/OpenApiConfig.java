package com.fastfood.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FastFood Service API")
                        .version("1.0.0")
                        .description("Sistema de pedidos de comida rápida con mecanismo de rollback")
                        .contact(new Contact()
                                .name("FastFood Service")
                                .email("support@fastfood.com")));
    }
}
