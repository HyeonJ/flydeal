package com.flydeal.backend.controller;

import com.flydeal.backend.dto.FlightSearchRequest;
import com.flydeal.backend.dto.FlightSearchResponse;
import com.flydeal.backend.service.FlightSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class FlightSearchController {

    private final FlightSearchService flightSearchService;

    @GetMapping("/flights")
    public ResponseEntity<FlightSearchResponse> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDate,
            @RequestParam(required = false) String returnDate,
            @RequestParam(defaultValue = "1") int passengers
    ) {
        FlightSearchRequest request = new FlightSearchRequest();
        request.setOrigin(origin);
        request.setDestination(destination);
        request.setDepartureDate(departureDate);
        request.setReturnDate(returnDate);
        request.setPassengers(passengers);

        return ResponseEntity.ok(flightSearchService.search(request));
    }
}
