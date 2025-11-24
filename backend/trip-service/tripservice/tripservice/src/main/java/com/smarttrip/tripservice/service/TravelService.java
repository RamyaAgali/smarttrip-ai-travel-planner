package com.smarttrip.tripservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.*;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Service
public class TravelService {

    private final WebClient webClient;

    @Value("${geoapify.key}")
    private String geoapifyKey;

    @Value("${openrouteservice.key}")
    private String orsKey;

    public TravelService(WebClient.Builder customWebClientBuilder) {
        this.webClient = customWebClientBuilder
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 10 sec connection timeout
                                .responseTimeout(Duration.ofSeconds(10))             // 10 sec response timeout
                                .doOnConnected(conn -> 
                                        conn.addHandlerLast(new ReadTimeoutHandler(10))
                                            .addHandlerLast(new WriteTimeoutHandler(10))
                                )
                ))
                .build();
    }

    // ✅ Fetch places from Geoapify
    public List<Map<String, Object>> fetchPlaces(double lon, double lat, String category) {
        try {
            String url = "https://api.geoapify.com/v2/places?categories=" + category +
                    "&filter=circle:" + lon + "," + lat + ",5000&limit=10&apiKey=" + geoapifyKey;

            Map response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("features")) return List.of();
            List<Map<String, Object>> features = (List<Map<String, Object>>) response.get("features");

            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> f : features) {
                Map<String, Object> props = (Map<String, Object>) f.get("properties");
                Map<String, Object> geom = (Map<String, Object>) f.get("geometry");
                List coords = (List) geom.get("coordinates");

                if (coords != null && coords.size() >= 2) {
                    double placeLon = ((Number) coords.get(0)).doubleValue();
                    double placeLat = ((Number) coords.get(1)).doubleValue();
                    String name = String.valueOf(props.getOrDefault("name", "Unknown"));
                    String address = String.valueOf(props.getOrDefault("formatted", ""));
                    result.add(Map.of(
                            "name", name,
                            "address", address,
                            "lon", placeLon,
                            "lat", placeLat
                    ));
                }
            }
            return result;

        } catch (Exception e) {
            System.err.println("Failed to fetch places: " + e.getMessage());
            return List.of();
        }
    }

    public double getDistance(double lon1, double lat1, double lon2, double lat2) {
        try {
            String url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + orsKey;

            Map<String, Object> body = Map.of(
                    "coordinates", List.of(
                            List.of(lon1, lat1),
                            List.of(lon2, lat2)
                    )
            );

            Map response = webClient.post()
                    .uri(url)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || !response.containsKey("features")) return 0;
            List<Map> features = (List<Map>) response.get("features");
            if (features.isEmpty()) return 0;

            Map props = (Map) ((Map) features.get(0).get("properties")).get("summary");
            double dist = ((Number) props.get("distance")).doubleValue() / 1000.0;
            return Math.round(dist * 100.0) / 100.0;

        } catch (Exception e) {
            System.err.println("Distance fetch failed: " + e.getMessage());
            return 0;
        }
    }
}