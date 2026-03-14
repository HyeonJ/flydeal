package com.flydeal.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FlightSearchResponse {
    private List<FlightOffer> offers;

    @Data
    @Builder
    public static class FlightOffer {
        private String offerId;
        private String totalAmount;
        private String totalCurrency;
        private String airline;
        private String departureTime;
        private String arrivalTime;
        private String origin;
        private String destination;
        private int stops;
    }
}
