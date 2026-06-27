package com.aims.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "Bearer Authentication";

    @Bean
    public OpenAPI aimsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AIMS Backend API")
                        .description("""
                                API documentation for the AIMS e-commerce system (Group 03 - ITSS).

                                Most customer endpoints under /api/** are public.
                                Manager and admin endpoints require a JWT from POST /auth/login.
                                Use the Authorize button to attach Bearer tokens when testing protected routes.""")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AIMS Team 03")
                                .email("group03@aims.local")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token returned by POST /auth/login")));
    }
}
