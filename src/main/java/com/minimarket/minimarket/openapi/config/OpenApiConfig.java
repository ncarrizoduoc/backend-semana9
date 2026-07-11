package com.minimarket.minimarket.openapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;

import org.springframework.beans.factory.annotation.Value;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Autenticacion y autorizacion por roles basada en JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("0.0.1-SNAPSHOT") String appVersion){
        return new OpenAPI()
            .info(new Info()
                .title("Minimarket")
                .version(appVersion)
                .description("Sistema de gestion de usuarios, productos y ventas de minimarket")
            );
    }

}
