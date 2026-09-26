package com.aimemory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8081}")
    private String serverPort;

    @Bean
    public OpenAPI aiMemoryOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("AI Memory API")
                        .description("""
                    Persistent memory as a service for LLM applications.

                    Plug memory into any chatbot with a simple REST API.
                    Extract facts from conversations automatically, store them,
                    and retrieve relevant context in milliseconds.
                    """)
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("AI Memory Team")
                                .email("hello@aimemory.dev"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local dev")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("API Key")
                                        .description("""
                            API key no formato `amk_test_...` (sandbox) ou `amk_live_...` (produção).
                            Envie no header: `Authorization: Bearer amk_test_...`
                            """)))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}