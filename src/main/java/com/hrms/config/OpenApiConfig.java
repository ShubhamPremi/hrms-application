package com.hrms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI hrmsOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("HRMS API")
                        .description("""
                                Enterprise Human Resource Management System API.
                                
                                **Authentication:** All endpoints except /api/v1/auth/** require
                                a Bearer JWT token. Use the /api/v1/auth/login endpoint to obtain
                                a token, then click the Authorize button above and enter:
                                Bearer {your_token}
                                """)
                        .version("v1.0")
                        .contact(new Contact()
                                .name("HRMS Team")
                                .email("admin@hrms.com"))
                        .license(new License()
                                .name("MIT License")))
                // Adds a global "Authorize" button to Swagger UI
                // Users paste their JWT once and all requests include it
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")));
    }
}
