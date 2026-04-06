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

    @Value("${kiwi.api-key:}")
    private String kiwiApiKey;

    @Value("${kiwi.base-url}")
    private String kiwiBaseUrl;

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
    public RestClient kiwiRestClient() {
        return RestClient.builder()
                .baseUrl(kiwiBaseUrl)
                .defaultHeader("apikey", kiwiApiKey)
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
