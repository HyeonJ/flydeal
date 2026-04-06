package com.flydeal.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${duffel.api-token}")
    private String duffelApiToken;

    @Value("${duffel.base-url}")
    private String duffelBaseUrl;

    @Value("${serpapi.api-key:}")
    private String serpApiKey;

    @Value("${serpapi.base-url}")
    private String serpApiBaseUrl;

    @Bean
    public RestClient duffelRestClient() {
        return RestClient.builder()
                .baseUrl(duffelBaseUrl)
                .defaultHeader("Authorization", "Bearer " + duffelApiToken)
                .defaultHeader("Duffel-Version", "v2")
                .requestFactory(clientHttpRequestFactory())
                .build();
    }

    @Bean
    public RestClient serpApiRestClient() {
        return RestClient.builder()
                .baseUrl(serpApiBaseUrl)
                .requestFactory(clientHttpRequestFactory())
                .build();
    }

    private SimpleClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return factory;
    }
}
