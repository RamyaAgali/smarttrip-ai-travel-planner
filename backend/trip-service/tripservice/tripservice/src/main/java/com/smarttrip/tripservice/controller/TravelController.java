package com.smarttrip.tripservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/travel")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
public class TravelController {

    private final WebClient webClient;

    @Value("${geoapify.key}")
    private String geoapifyKey;

    @Value("${openrouteservice.key}")
    private String orsKey;

    @Value("${exchange.url}")
    private String exchangeUrl;

    @Value("${weather.url}")
    private String weatherUrl;
    
    @Value("${openrouter.key}")
    private String openRouterKey;

    public TravelController(WebClient.Builder builder) {
        this.webClient = builder
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    /* ====== Models ====== */
    record Place(String name, double lat, double lon, String address) {}
    record Expense(String name, double value, String currency) {}
    record DayPlan(int day, List<String> activities) {}
    record Weather(double temperature, double windspeed, String condition) {}

    record PlanResponse(
            String from,
            String destination,
            Weather weather,
            List<Place> attractions,
            List<Place> restaurants,
            List<Expense> expenses,
            List<DayPlan> days,
            double totalDistance
    ) {}

    /* ====== Main endpoint ====== */
    @GetMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PlanResponse> plan(
            @RequestParam(required = false) String from,
            @RequestParam String destination,
            @RequestParam(defaultValue = "3") int days,
            @RequestParam(defaultValue = "USD") String currency
    ) {
        String encodedDest = URLEncoder.encode(destination, StandardCharsets.UTF_8);
        String geocodeDestUrl = "https://api.geoapify.com/v1/geocode/search?text=" + encodedDest + "&limit=1&apiKey=" + geoapifyKey;

        return webClient.get().uri(geocodeDestUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(geo -> {
                    Map first = firstFeature(geo);
                    if (first == null) return Mono.error(new RuntimeException("Destination not found"));

                    Map props = (Map) first.get("properties");
                    double destLat = toDouble(props.get("lat"));
                    double destLon = toDouble(props.get("lon"));
                    String country = safeString(props.get("country"));

                    /* 🌦 Fetch weather data */
                    Mono<Weather> weatherMono = webClient.get()
                            .uri(weatherUrl + "?latitude=" + destLat + "&longitude=" + destLon + "&current_weather=true")
                            .retrieve()
                            .bodyToMono(Map.class)
                            .map(data -> {
                                Map current = (Map) data.get("current_weather");
                                if (current == null)
                                    return new Weather(0, 0, "Unknown");
                                return new Weather(
                                        toDouble(current.get("temperature")),
                                        toDouble(current.get("windspeed")),
                                        decodeWeatherCode(((Number) current.get("weathercode")).intValue())
                                );
                            })
                            .onErrorReturn(new Weather(0, 0, "Unavailable"));

                    /* 🏞 Fetch places */
                    String attrCats = "tourism.sights,tourism.attraction,natural,entertainment";
                    String restCats = "catering.restaurant,catering.fast_food,catering.cafe";

                    String attractionsUrl = "https://api.geoapify.com/v2/places?categories=" + attrCats +
                            "&filter=circle:" + destLon + "," + destLat + ",15000&limit=20&apiKey=" + geoapifyKey;

                    String restaurantsUrl = "https://api.geoapify.com/v2/places?categories=" + restCats +
                            "&filter=circle:" + destLon + "," + destLat + ",15000&limit=20&apiKey=" + geoapifyKey;

                    Mono<List<Place>> attractionsMono = fetchPlaces(attractionsUrl);
                    Mono<List<Place>> restaurantsMono = fetchPlaces(restaurantsUrl);

                    /* 🚗 Distance (optional) */
                    Mono<Double> distanceMono;
                    if (from != null && !from.isBlank()) {
                        String encodedFrom = URLEncoder.encode(from, StandardCharsets.UTF_8);
                        String geocodeFromUrl = "https://api.geoapify.com/v1/geocode/search?text=" + encodedFrom + "&limit=1&apiKey=" + geoapifyKey;

                        distanceMono = webClient.get().uri(geocodeFromUrl)
                                .retrieve()
                                .bodyToMono(Map.class)
                                .flatMap(fromGeo -> {
                                    Map fromFeature = firstFeature(fromGeo);
                                    if (fromFeature == null) return Mono.just(-1.0);
                                    Map fromProps = (Map) fromFeature.get("properties");
                                    double fromLat = toDouble(fromProps.get("lat"));
                                    double fromLon = toDouble(fromProps.get("lon"));

                                    return orsDistanceKm(fromLon, fromLat, destLon, destLat)
                                            .onErrorResume(e -> Mono.just(haversineKm(fromLat, fromLon, destLat, destLon)));
                                });
                    } else {
                        distanceMono = Mono.just(-1.0); // No origin provided
                    }

                    /* 💰 Expenses */
                    Mono<List<Expense>> expensesMono = estimateExpenses(country, days, currency);

                    /* 📅 Day plan */
                    Mono<List<DayPlan>> dayPlanMono = attractionsMono.map(list -> generateDayPlan(list, days));

                    return Mono.zip(weatherMono, attractionsMono, restaurantsMono, expensesMono, dayPlanMono, distanceMono)
                            .map(tuple -> new PlanResponse(
                                    from,
                                    destination,
                                    tuple.getT1(),
                                    tuple.getT2(),
                                    tuple.getT3(),
                                    tuple.getT4(),
                                    tuple.getT5(),
                                    round2(tuple.getT6())
                                    
                            ));
                });
    }
    @PostMapping(value = "/cost", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> estimateCostWithAI(@RequestBody Map<String, Object> request) {
        String destination = safeString(request.get("destination"));
        String from = safeString(request.get("from"));
        int days = request.get("days") != null ? Integer.parseInt(request.get("days").toString()) : 3;
        int travelers = request.get("travelers") != null
                ? Integer.parseInt(request.get("travelers").toString())
                : 1;
        String currency = safeString(request.get("currency"));

        // 🧠 Better prompt
        String prompt = String.format(
            "Estimate realistic total travel costs for %d travelers spending %d days from %s to %s in %s currency. " +
            "Include categories: Stay, Food, Transport, and Activities. " +
            "Base your estimate on common mid-range travel prices. " +
            "Return ONLY a pure JSON array like: " +
            "[{\"name\":\"Stay\",\"value\":...},{\"name\":\"Food\",\"value\":...},{\"name\":\"Transport\",\"value\":...},{\"name\":\"Activities\",\"value\":...}]. " +
            "Do not include any explanation or text outside the JSON.",
            travelers, days, from.isBlank() ? "user's location" : from, destination, currency
        );

        Map<String, Object> body = Map.of(
            "model", "gpt-4o-mini",
            "messages", List.of(
                Map.of("role", "system", "content", "You are a precise travel cost estimation AI that returns ONLY JSON."),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.7
        );

        String apiKey = openRouterKey;
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("⚠ Missing OpenRouter API key — set OPENROUTER_API_KEY in environment!");
            return Mono.just(Map.of("error", "Missing OpenRouter API key"));
        }

        System.out.println("📡 Calling OpenRouter for cost estimation...");

        return webClient.post()
                .uri("https://openrouter.ai/api/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> {
                    try {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
                        if (choices == null || choices.isEmpty())
                            return Map.<String, Object>of("error", "No AI response received");

                        Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                        String content = safeString(msg.get("content"));

                        // 🧹 Extract only JSON using regex
                        String cleaned = content.replaceAll("(?s).?(\\[\\{.\\}\\]).*", "$1").trim();

                        return Map.<String, Object>of("cost_estimate", cleaned);
                    } catch (Exception e) {
                        return Map.<String, Object>of("error", "Failed to parse AI response: " + e.getMessage());
                    }
                })
                .onErrorResume(e ->
                    Mono.just(Map.<String, Object>of("error", "OpenRouter API call failed: " + e.getMessage()))
                );
    }
    
    /* ====== Helpers ====== */
    private Mono<List<Place>> fetchPlaces(String url) {
        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(m -> {
                    Object feats = m.get("features");
                    if (!(feats instanceof List<?> list)) return List.of();
                    List<Place> out = new ArrayList<>();
                    for (Object o : list) {
                        if (!(o instanceof Map<?, ?> f)) continue;
                        Map props = (Map) f.get("properties");
                        Map geom = (Map) f.get("geometry");
                        String name = safeString(props.get("name"));
                        if (name.isBlank()) continue;
                        String address = safeString(props.get("formatted"));
                        List coords = (List) geom.get("coordinates");
                        if (coords == null || coords.size() < 2) continue;
                        double lon = toDouble(coords.get(0));
                        double lat = toDouble(coords.get(1));
                        out.add(new Place(name, lat, lon, address));
                    }
                    return out;
                });
    }

    private Mono<Double> orsDistanceKm(double fromLon, double fromLat, double toLon, double toLat) {
        String url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + orsKey;
        Map<String, Object> body = Map.of("coordinates", List.of(
                List.of(fromLon, fromLat), List.of(toLon, toLat)
        ));
        return webClient.post().uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(res -> {
                    List<Map> feats = (List<Map>) res.get("features");
                    if (feats == null || feats.isEmpty()) return haversineKm(fromLat, fromLon, toLat, toLon);
                    Map props = (Map) feats.get(0).get("properties");
                    Map summary = (Map) props.get("summary");
                    double meters = toDouble(summary.get("distance"));
                    return meters / 1000.0;
                });
    }
	  private Mono<List<Expense>> estimateExpenses(String country, int days, String currency) {
	  double stay = 50, food = 20, transport = 10, activities = 15;
	  String c = country == null ? "" : country.toLowerCase();
	
	  if (c.contains("india")) { stay = 25; food = 10; transport = 5; activities = 7; }
	  else if (c.contains("germany")) { stay = 80; food = 35; transport = 20; activities = 30; }
	  else if (c.contains("japan")) { stay = 110; food = 45; transport = 30; activities = 45; }
	
	  String url = exchangeUrl + "&base=USD"; // ✅ using exchangerate.host with key
	
	  Mono<Double> fxMono = webClient.get().uri(url)
	          .retrieve()
	          .bodyToMono(Map.class)
	          .map(json -> {
	              Map<String, Object> rates = (Map<String, Object>) json.get("rates");
	              if (rates != null && rates.containsKey(currency))
	                  return toDouble(rates.get(currency));
	              return 1.0;
	          })
	          .onErrorReturn(1.0);
	
	  double finalStay = stay * days, finalFood = food * days, finalTransport = transport * days, finalAct = activities * days;
	  return fxMono.map(rate -> List.of(
	          new Expense("Stay", round0(finalStay * rate), currency),
	          new Expense("Food", round0(finalFood * rate), currency),
	          new Expense("Transport", round0(finalTransport * rate), currency),
	          new Expense("Activities", round0(finalAct * rate), currency)
	  ));
	}
	
	private List<DayPlan> generateDayPlan(List<Place> attractions, int days) {
	  List<DayPlan> plans = new ArrayList<>();
	  int i = 0;
	  for (int d = 1; d <= days; d++) {
	      List<String> acts = new ArrayList<>();
	      for (int j = 0; j < 2 && i < attractions.size(); j++, i++) {
	          acts.add("Visit " + attractions.get(i).name());
	      }
	      if (acts.isEmpty()) acts.add("Relax and explore nearby cafés");
	      plans.add(new DayPlan(d, acts));
	  }
	  return plans;
	}
    // All your old helper methods below remain the same
    // (estimateExpenses, generateDayPlan, rounders, haversine, decodeWeatherCode, etc.)
    // No need to modify them.

    private static Map firstFeature(Map root) {
        if (root == null) return null;
        Object f = root.get("features");
        if (f instanceof List<?> list && !list.isEmpty())
            if (list.get(0) instanceof Map<?, ?> m) return (Map) m;
        return null;
    }

    private static String safeString(Object o) { return o == null ? "" : String.valueOf(o); }
    private static double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return 0; }
    }
    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private static double round0(double v) { return Math.round(v); }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String decodeWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Clear Sky";
            case 1, 2, 3 -> "Partly Cloudy";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 71, 73, 75 -> "Snow";
            case 95 -> "Thunderstorm";
            default -> "Unknown";
        };
    }
}