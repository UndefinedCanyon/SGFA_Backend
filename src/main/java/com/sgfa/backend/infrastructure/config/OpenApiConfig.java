package com.sgfa.backend.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sgfaOpenAPI() {
        final String esquemaSeguridad = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("SGFA - API REST")
                        .description("Software de Gestión para Ferias Artesanales de Fusagasugá")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(esquemaSeguridad))
                .components(new Components()
                        .addSecuritySchemes(esquemaSeguridad, new SecurityScheme()
                                .name(esquemaSeguridad)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}