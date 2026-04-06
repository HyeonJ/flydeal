package com.flydeal.backend.client;

import com.flydeal.backend.dto.FlightOffer;
import com.flydeal.backend.dto.FlightSearchRequest;

import java.util.List;

public interface LccClient {

    List<FlightOffer> searchFlights(FlightSearchRequest request);
}
