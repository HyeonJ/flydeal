package com.flydeal.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Builder
public class FlightOffer {

    private String id;
    private String source;
    private String airline;
    private String airlineLogo;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String origin;
    private String destination;
    private long durationMinutes;
    private int stops;
    private BigDecimal price;
    private String currency;
    private String deepLink;
}
