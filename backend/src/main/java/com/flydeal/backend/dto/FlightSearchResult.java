package com.flydeal.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FlightSearchResult {

    private List<FlightOffer> offers;
    private List<String> sources;
    private boolean cached;
    private int totalCount;
    private List<String> warnings;
}
