package com.innowise.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${app.url:}")
    private String appUrl;

    @Bean
    public OpenAPI authServiceOpenApi() {
        OpenAPI openAPI = new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("Auth Service API")
                        .version("v1"));

        if (!appUrl.isBlank()) {
            openAPI.servers(List.of(new Server()
                    .url(appUrl)
                    .description("Public Gateway URL")));
        }

        return openAPI;
    }
}
