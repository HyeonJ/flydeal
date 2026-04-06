package com.flydeal.backend.client;

import com.flydeal.backend.dto.FlightOffer;
import com.flydeal.backend.dto.FlightSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "serpapi.mock", havingValue = "false")
@RequiredArgsConstructor
public class SerpApiClient implements LccClient {

    private final RestClient serpApiRestClient;

    @Override
    public List<FlightOffer> searchFlights(FlightSearchRequest request) {
        // TODO: 실제 SerpApi Google Flights 연동
        log.warn("[SerpApi] 실제 API 호출 — 아직 미구현");
        throw new UnsupportedOperationException("SerpApi 실제 연동 미구현");
    }
}
