package com.flydeal.backend.service;

import com.flydeal.backend.client.DuffelApiClient;
import com.flydeal.backend.dto.FlightSearchRequest;
import com.flydeal.backend.dto.FlightSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FlightSearchService {

    private final DuffelApiClient duffelApiClient;

    public FlightSearchResponse search(FlightSearchRequest request) {
        Map<String, Object> apiResponse = duffelApiClient.searchOffers(request);

        List<FlightSearchResponse.FlightOffer> offers = new ArrayList<>();

        if (apiResponse != null && apiResponse.containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) apiResponse.get("data");
            List<Map<String, Object>> rawOffers = (List<Map<String, Object>>) data.get("offers");

            if (rawOffers != null) {
                for (Map<String, Object> raw : rawOffers) {
                    offers.add(mapOffer(raw));
                }
            }
        }

        return FlightSearchResponse.builder()
                .offers(offers)
                .build();
    }

    private FlightSearchResponse.FlightOffer mapOffer(Map<String, Object> raw) {
        List<Map<String, Object>> slices = (List<Map<String, Object>>) raw.get("slices");

        String airline = "";
        String departureTime = "";
        String arrivalTime = "";
        String origin = "";
        String destination = "";
        int stops = 0;

        if (slices != null && !slices.isEmpty()) {
            Map<String, Object> firstSlice = slices.get(0);
            List<Map<String, Object>> segments = (List<Map<String, Object>>) firstSlice.get("segments");
            stops = segments != null ? Math.max(0, segments.size() - 1) : 0;

            if (segments != null && !segments.isEmpty()) {
                Map<String, Object> firstSeg = segments.get(0);
                Map<String, Object> lastSeg = segments.get(segments.size() - 1);

                Map<String, Object> carrier = (Map<String, Object>) firstSeg.get("marketing_carrier");
                if (carrier != null) {
                    airline = (String) carrier.get("name");
                }

                Map<String, Object> originMap = (Map<String, Object>) firstSeg.get("origin");
                if (originMap != null) origin = (String) originMap.get("iata_code");

                Map<String, Object> destMap = (Map<String, Object>) lastSeg.get("destination");
                if (destMap != null) destination = (String) destMap.get("iata_code");

                departureTime = (String) firstSeg.get("departing_at");
                arrivalTime = (String) lastSeg.get("arriving_at");
            }
        }

        return FlightSearchResponse.FlightOffer.builder()
                .offerId((String) raw.get("id"))
                .totalAmount((String) raw.get("total_amount"))
                .totalCurrency((String) raw.get("total_currency"))
                .airline(airline)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .origin(origin)
                .destination(destination)
                .stops(stops)
                .build();
    }
}
