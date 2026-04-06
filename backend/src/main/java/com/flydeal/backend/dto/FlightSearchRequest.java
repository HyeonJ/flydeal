package com.flydeal.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FlightSearchRequest {

    @NotBlank
    @Size(min = 3, max = 3)
    private String origin;

    @NotBlank
    @Size(min = 3, max = 3)
    private String destination;

    @NotNull
    private LocalDate departureDate;

    private LocalDate returnDate;

    @NotBlank
    private String flightType = "round";

    @Min(1)
    private int passengers = 1;

    @NotBlank
    private String cabinClass = "economy";
}
