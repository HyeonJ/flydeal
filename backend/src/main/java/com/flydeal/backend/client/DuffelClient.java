package com.flydeal.backend.client;

import com.flydeal.backend.dto.FlightOffer;
import com.flydeal.backend.dto.FlightSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuffelClient {

    private final RestClient duffelRestClient;

    public List<FlightOffer> searchFlights(FlightSearchRequest request) {
        log.info("[searchFlights] origin={}, destination={}, departureDate={}",
                request.getOrigin(), request.getDestination(), request.getDepartureDate());

        Map<String, Object> body = buildRequestBody(request);

        Map<String, Object> response = duffelRestClient.post()
                .uri("/air/offer_requests?return_offers=true")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        log.info("[searchFlights] Duffel 응답 수신 완료");
        return convertOffers(response);
    }

    private Map<String, Object> buildRequestBody(FlightSearchRequest request) {
        List<Map<String, Object>> slices = new ArrayList<>();

        Map<String, Object> outboundSlice = new HashMap<>();
        outboundSlice.put("origin", request.getOrigin());
        outboundSlice.put("destination", request.getDestination());
        outboundSlice.put("departure_date", request.getDepartureDate().toString());
        slices.add(outboundSlice);

        if (request.getReturnDate() != null) {
            Map<String, Object> returnSlice = new HashMap<>();
            returnSlice.put("origin", request.getDestination());
            returnSlice.put("destination", request.getOrigin());
            returnSlice.put("departure_date", request.getReturnDate().toString());
            slices.add(returnSlice);
        }

        List<Map<String, String>> passengers = new ArrayList<>();
        for (int i = 0; i < request.getPassengers(); i++) {
            passengers.add(Map.of("type", "adult"));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("slices", slices);
        data.put("passengers", passengers);
        data.put("cabin_class", request.getCabinClass());

        return Map.of("data", data);
    }

    @SuppressWarnings("unchecked")
    private List<FlightOffer> convertOffers(Map<String, Object> response) {
        List<FlightOffer> results = new ArrayList<>();
        if (response == null || !response.containsKey("data")) {
            return results;
        }

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        List<Map<String, Object>> offers = (List<Map<String, Object>>) data.get("offers");
        if (offers == null) {
            return results;
        }

        for (Map<String, Object> offer : offers) {
            try {
                results.add(convertSingleOffer(offer));
            } catch (Exception e) {
                log.error("[convertOffers] offer 변환 실패: id={}", offer.get("id"), e);
            }
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private FlightOffer convertSingleOffer(Map<String, Object> raw) {
        List<Map<String, Object>> slices = (List<Map<String, Object>>) raw.get("slices");
        Map<String, Object> firstSlice = slices.get(0);
        List<Map<String, Object>> segments = (List<Map<String, Object>>) firstSlice.get("segments");

        Map<String, Object> firstSegment = segments.get(0);
        Map<String, Object> lastSegment = segments.get(segments.size() - 1);

        Map<String, Object> owner = (Map<String, Object>) raw.get("owner");
        String airline = owner != null ? (String) owner.get("name") : "";
        String airlineLogo = owner != null ? (String) owner.get("logo_symbol_url") : null;

        Map<String, Object> originMap = (Map<String, Object>) firstSegment.get("origin");
        Map<String, Object> destMap = (Map<String, Object>) lastSegment.get("destination");

        String departureAt = (String) firstSegment.get("departing_at");
        String arrivingAt = (String) lastSegment.get("arriving_at");

        LocalDateTime departureTime = LocalDateTime.parse(departureAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime arrivalTime = LocalDateTime.parse(arrivingAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String durationStr = (String) firstSlice.get("duration");
        long durationMinutes = parseDuration(durationStr);

        return FlightOffer.builder()
                .id((String) raw.get("id"))
                .source("DUFFEL")
                .airline(airline)
                .airlineLogo(airlineLogo)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .origin((String) originMap.get("iata_code"))
                .destination((String) destMap.get("iata_code"))
                .durationMinutes(durationMinutes)
                .stops(segments.size() - 1)
                .price(new BigDecimal((String) raw.get("total_amount")))
                .currency((String) raw.get("total_currency"))
                .deepLink(null)
                .build();
    }

    private long parseDuration(String isoDuration) {
        if (isoDuration == null) {
            return 0;
        }
        try {
            return java.time.Duration.parse(isoDuration).toMinutes();
        } catch (Exception e) {
            return 0;
        }
    }
}
