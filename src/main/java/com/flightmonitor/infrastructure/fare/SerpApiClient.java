package com.flightmonitor.infrastructure.fare;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SerpApiClient {

    private static final Logger log = LoggerFactory.getLogger(SerpApiClient.class);

    private final RestClient restClient;
    private final SerpApiProperties properties;

    public SerpApiClient(SerpApiProperties properties) {
        this.properties  = properties;
        this.restClient  = RestClient.create();
    }

    @SuppressWarnings("unchecked")
    public Optional<BigDecimal> fetchLowestPrice(String origin, String destination, LocalDate date) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uri -> uri
                            .scheme("https")
                            .host("serpapi.com")
                            .path("/search")
                            .queryParam("engine",        "google_flights")
                            .queryParam("departure_id",  origin)
                            .queryParam("arrival_id",    destination)
                            .queryParam("outbound_date", date.toString())
                            .queryParam("currency",      "BRL")
                            .queryParam("type",          "2")
                            .queryParam("hl",            "en")
                            .queryParam("api_key",       properties.getApiKey())
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null) return Optional.empty();

            if (response.containsKey("error")) {
                log.warn("SerpApi error for {}->{}: {}", origin, destination, response.get("error"));
                return Optional.empty();
            }

            return extractLowestPrice(response, origin, destination, date);

        } catch (Exception e) {
            log.warn("SerpApi request failed for {}->{}: {}", origin, destination, e.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<BigDecimal> extractLowestPrice(Map<String, Object> response,
                                                     String origin, String destination, LocalDate date) {
        List<Map<String, Object>> flights = (List<Map<String, Object>>) response.get("best_flights");

        if (flights == null || flights.isEmpty()) {
            flights = (List<Map<String, Object>>) response.get("other_flights");
        }

        if (flights == null || flights.isEmpty()) {
            log.warn("No flights found for {}->{} on {}", origin, destination, date);
            return Optional.empty();
        }

        Object priceObj = flights.get(0).get("price");
        if (priceObj == null) return Optional.empty();

        return Optional.of(new BigDecimal(priceObj.toString()));
    }
}
