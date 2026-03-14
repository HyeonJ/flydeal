package com.flydeal.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DuffelConfig {

    @Value("${duffel.api.token}")
    private String apiToken;

    @Value("${duffel.api.base-url}")
    private String baseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
