package com.strengthhub.strength_hub_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StrengthHub API")
                        .version("1.0.0")
                        .description("Powerlifting coaching and programming platform API")
                        .contact(new Contact()
                                .name("Alp Karagöz")
                                .email("alpkaragoz3@gmail.com")));
    }
}
