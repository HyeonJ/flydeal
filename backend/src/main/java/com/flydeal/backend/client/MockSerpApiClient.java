package com.flydeal.backend.client;

import com.flydeal.backend.dto.FlightOffer;
import com.flydeal.backend.dto.FlightSearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@ConditionalOnProperty(name = "serpapi.mock", havingValue = "true", matchIfMissing = true)
public class MockSerpApiClient implements LccClient {

    private static final List<AirlineInfo> LCC_AIRLINES = List.of(
            new AirlineInfo("7C", "제주항공", "https://www.gstatic.com/flights/airline_logos/70px/7C.png"),
            new AirlineInfo("LJ", "진에어", "https://www.gstatic.com/flights/airline_logos/70px/LJ.png"),
            new AirlineInfo("TW", "티웨이항공", "https://www.gstatic.com/flights/airline_logos/70px/TW.png"),
            new AirlineInfo("BX", "에어부산", "https://www.gstatic.com/flights/airline_logos/70px/BX.png"),
            new AirlineInfo("RS", "에어서울", "https://www.gstatic.com/flights/airline_logos/70px/RS.png")
    );

    private static final List<RouteTemplate> ROUTES = List.of(
            new RouteTemplate("ICN", "NRT", 150, 280, 155),
            new RouteTemplate("ICN", "KIX", 130, 260, 140),
            new RouteTemplate("ICN", "FUK", 110, 230, 105),
            new RouteTemplate("ICN", "CTS", 170, 320, 195),
            new RouteTemplate("ICN", "BKK", 250, 480, 360),
            new RouteTemplate("ICN", "DAD", 220, 400, 300),
            new RouteTemplate("ICN", "CEB", 200, 380, 320),
            new RouteTemplate("ICN", "TPE", 160, 300, 165),
            new RouteTemplate("ICN", "HKG", 180, 350, 220),
            new RouteTemplate("GMP", "CJU", 50, 120, 65),
            new RouteTemplate("PUS", "NRT", 160, 290, 160),
            new RouteTemplate("PUS", "FUK", 90, 180, 80)
    );

    @Override
    public List<FlightOffer> searchFlights(FlightSearchRequest request) {
        log.info("[MockSerpApi] Mock 검색: {} → {}, {}", request.getOrigin(), request.getDestination(), request.getDepartureDate());

        List<FlightOffer> offers = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        RouteTemplate route = findRoute(request.getOrigin(), request.getDestination());
        int basePriceKrw = route != null ? route.basePriceKrw : 200_000;
        int durationMin = route != null ? route.durationMinutes : 180;

        int offerCount = random.nextInt(3, 7);

        for (int i = 0; i < offerCount; i++) {
            AirlineInfo airline = LCC_AIRLINES.get(random.nextInt(LCC_AIRLINES.size()));
            int priceVariation = random.nextInt(-30_000, 80_000);
            int price = (basePriceKrw + priceVariation) * request.getPassengers();
            int durationVariation = random.nextInt(-10, 30);
            int stops = random.nextInt(100) < 75 ? 0 : 1;

            LocalTime depTime = LocalTime.of(random.nextInt(6, 22), random.nextInt(0, 4) * 15);
            int actualDuration = durationMin + durationVariation + (stops * random.nextInt(60, 150));

            LocalDateTime departure = LocalDateTime.of(request.getDepartureDate(), depTime);
            LocalDateTime arrival = departure.plusMinutes(actualDuration);

            offers.add(FlightOffer.builder()
                    .id("serpapi-mock-" + request.getDepartureDate() + "-" + i)
                    .source("SERPAPI")
                    .airline(airline.name)
                    .airlineLogo(airline.logoUrl)
                    .departureTime(departure)
                    .arrivalTime(arrival)
                    .origin(request.getOrigin())
                    .destination(request.getDestination())
                    .durationMinutes(actualDuration)
                    .stops(stops)
                    .price(BigDecimal.valueOf(price))
                    .currency("KRW")
                    .deepLink(null)
                    .build());
        }

        if (request.getReturnDate() != null) {
            int returnCount = random.nextInt(2, 5);
            for (int i = 0; i < returnCount; i++) {
                AirlineInfo airline = LCC_AIRLINES.get(random.nextInt(LCC_AIRLINES.size()));
                int priceVariation = random.nextInt(-30_000, 80_000);
                int price = (basePriceKrw + priceVariation) * request.getPassengers();
                int durationVariation = random.nextInt(-10, 30);
                int stops = random.nextInt(100) < 75 ? 0 : 1;

                LocalTime depTime = LocalTime.of(random.nextInt(6, 22), random.nextInt(0, 4) * 15);
                int actualDuration = durationMin + durationVariation + (stops * random.nextInt(60, 150));

                LocalDateTime departure = LocalDateTime.of(request.getReturnDate(), depTime);
                LocalDateTime arrival = departure.plusMinutes(actualDuration);

                offers.add(FlightOffer.builder()
                        .id("serpapi-mock-ret-" + request.getReturnDate() + "-" + i)
                        .source("SERPAPI")
                        .airline(airline.name)
                        .airlineLogo(airline.logoUrl)
                        .departureTime(departure)
                        .arrivalTime(arrival)
                        .origin(request.getDestination())
                        .destination(request.getOrigin())
                        .durationMinutes(actualDuration)
                        .stops(stops)
                        .price(BigDecimal.valueOf(price))
                        .currency("KRW")
                        .deepLink(null)
                        .build());
            }
        }

        log.info("[MockSerpApi] Mock 결과 {}건 생성", offers.size());
        return offers;
    }

    private RouteTemplate findRoute(String origin, String destination) {
        for (RouteTemplate route : ROUTES) {
            if (route.origin.equals(origin) && route.destination.equals(destination)) {
                return route;
            }
            if (route.origin.equals(destination) && route.destination.equals(origin)) {
                return new RouteTemplate(destination, origin, route.basePriceKrw, route.maxPriceKrw, route.durationMinutes);
            }
        }
        return null;
    }

    private record AirlineInfo(String code, String name, String logoUrl) {}

    private record RouteTemplate(String origin, String destination, int basePriceKrw, int maxPriceKrw, int durationMinutes) {
        RouteTemplate {
            basePriceKrw *= 1000;
            maxPriceKrw *= 1000;
        }
    }
}
