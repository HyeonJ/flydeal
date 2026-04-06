package com.flydeal.backend.service;

import com.flydeal.backend.client.DuffelClient;
import com.flydeal.backend.dto.FlightOffer;
import com.flydeal.backend.dto.FlightSearchRequest;
import com.flydeal.backend.dto.FlightSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightSearchService {

    private final DuffelClient duffelClient;

    public FlightSearchResult search(FlightSearchRequest request) {
        log.info("[search] origin={}, destination={}, departureDate={}, returnDate={}",
                request.getOrigin(), request.getDestination(),
                request.getDepartureDate(), request.getReturnDate());

        List<String> sources = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        CompletableFuture<List<FlightOffer>> duffelFuture = CompletableFuture
                .supplyAsync(() -> duffelClient.searchFlights(request))
                .orTimeout(15, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.error("[search] Duffel API 실패", ex);
                    warnings.add("Duffel API 일시 장애로 FSC 항공편이 포함되지 않았습니다");
                    return List.of();
                });

        List<FlightOffer> duffelOffers = duffelFuture.join();
        if (!duffelOffers.isEmpty()) {
            sources.add("DUFFEL");
        }

        List<FlightOffer> merged = new ArrayList<>(duffelOffers);

        List<FlightOffer> deduplicated = deduplicate(merged);
        deduplicated.sort(Comparator.comparing(FlightOffer::getPrice));

        return FlightSearchResult.builder()
                .offers(deduplicated)
                .sources(sources)
                .cached(false)
                .totalCount(deduplicated.size())
                .warnings(warnings.isEmpty() ? null : warnings)
                .build();
    }

    private List<FlightOffer> deduplicate(List<FlightOffer> offers) {
        Set<String> seen = new HashSet<>();
        List<FlightOffer> result = new ArrayList<>();

        for (FlightOffer offer : offers) {
            String key = deduplicationKey(offer);
            if (seen.add(key)) {
                result.add(offer);
            }
        }

        return result;
    }

    private String deduplicationKey(FlightOffer offer) {
        return offer.getAirline()
                + "|" + offer.getDepartureTime().truncatedTo(ChronoUnit.MINUTES)
                + "|" + offer.getArrivalTime().truncatedTo(ChronoUnit.MINUTES)
                + "|" + offer.getOrigin()
                + "|" + offer.getDestination();
    }
}
