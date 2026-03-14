package com.flydeal.backend.client;

import com.flydeal.backend.config.DuffelConfig;
import com.flydeal.backend.dto.FlightSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DuffelApiClient {

    private final RestTemplate restTemplate;
    private final DuffelConfig duffelConfig;

    public Map<String, Object> searchOffers(FlightSearchRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(duffelConfig.getApiToken());
        headers.set("Duffel-Version", "v2");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> slice = new HashMap<>();
        slice.put("origin", request.getOrigin());
        slice.put("destination", request.getDestination());
        slice.put("departure_date", request.getDepartureDate());

        List<Map<String, Object>> slices = new ArrayList<>();
        slices.add(slice);

        List<Map<String, Object>> passengers = new ArrayList<>();
        for (int i = 0; i < request.getPassengers(); i++) {
            Map<String, Object> passenger = new HashMap<>();
            passenger.put("type", "adult");
            passengers.add(passenger);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("slices", slices);
        data.put("passengers", passengers);

        Map<String, Object> body = new HashMap<>();
        body.put("data", data);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                duffelConfig.getBaseUrl() + "/air/offer_requests?return_offers=true",
                HttpMethod.POST,
                entity,
                Map.class
        );

        return response.getBody();
    }
}
