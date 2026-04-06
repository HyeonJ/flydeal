package com.flydeal.backend.controller;

import com.flydeal.backend.dto.ApiResponse;
import com.flydeal.backend.dto.FlightSearchRequest;
import com.flydeal.backend.dto.FlightSearchResult;
import com.flydeal.backend.service.FlightSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightSearchController {

    private final FlightSearchService flightSearchService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<FlightSearchResult>> searchFlights(
            @Valid @RequestBody FlightSearchRequest request) {
        log.info("[POST /api/flights/search] origin={}, destination={}, departureDate={}",
                request.getOrigin(), request.getDestination(), request.getDepartureDate());

        FlightSearchResult result = flightSearchService.search(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
