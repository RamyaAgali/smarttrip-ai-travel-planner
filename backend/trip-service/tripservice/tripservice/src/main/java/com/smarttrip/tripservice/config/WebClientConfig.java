package com.smarttrip.tripservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // ✅ Define a WebClient bean so that Spring can inject it anywhere
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
