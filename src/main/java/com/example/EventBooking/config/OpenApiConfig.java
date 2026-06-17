package com.example.EventBooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
        .info(new Info()
        .title("Event Booking REST API")
            .version("1.1.0")
            .description("Backend service to manage event creating, scheduling and track seat capacity amongst other things...."));
    }
}
