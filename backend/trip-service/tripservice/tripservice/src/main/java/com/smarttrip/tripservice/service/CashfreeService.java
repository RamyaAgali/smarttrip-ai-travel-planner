package com.smarttrip.tripservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class CashfreeService {

    private final WebClient cashfreeClient;
    private final WebClient internalAuthClient;
    private final String internalAuthToken;

    public CashfreeService(
            @Value("${cashfree.app.id}") String appId,
            @Value("${cashfree.secret.key}") String secretKey,
            @Value("${cashfree.base.url}") String baseUrl,
            @Value("${internal.base-url}") String internalAuthBaseUrl,
            @Value("${internal.auth.token}") String internalAuthToken
    ) {
        this.internalAuthToken = internalAuthToken;

        // ✅ Cashfree WebClient
        this.cashfreeClient = WebClient.builder()
                //.baseUrl(baseUrl)
        		.baseUrl("https://sandbox.cashfree.com")
                .defaultHeader("x-client-id", appId)
                .defaultHeader("x-client-secret", secretKey)
                //.defaultHeader("Authorization", "Bearer " + secretKey)// Added Header
                .defaultHeader("x-api-version", "2022-09-01")
                .defaultHeader("Content-Type", "application/json")
                .build();

        // ✅ Internal AuthService WebClient
        this.internalAuthClient = WebClient.builder()
                .baseUrl(internalAuthBaseUrl)
                .defaultHeader("Authorization", "Bearer " + internalAuthToken)
                .build();
    }

    /**
     * 🔹 Step 1: Securely fetch user details from AuthService (real data from DB)
     */
    private Mono<Map> fetchUserDetails(String username) {
        String realEmail =username.replace("__", ".").replace("_", "@");
        String url = "/api/auth/user/" + realEmail;
        return internalAuthClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    System.err.println("⚠ Failed to fetch user details: " + e.getMessage());
                    Map<String, Object> fallback = new HashMap<>();
                    fallback.put("name", "Unknown User");
                    fallback.put("email", "unknown@example.com");
                    fallback.put("mobileNumber", "9999999999");
                    return Mono.just(fallback);
                });
    }

    /**
     * 🔹 Step 2: Create Cashfree Order (with real user data)
     */
    public Mono<Map> createOrder(String orderId, Double amount, String currency, String username) {
        return fetchUserDetails(username).flatMap(user -> {
            String userName = (String) user.getOrDefault("name", "User");
            String userEmail = (String) user.getOrDefault("email", "unknown@example.com");
            String userPhone = (String) user.getOrDefault("mobileNumber", "9999999999");
            if(userPhone == null || !userPhone.matches("\\d{10}")) {
            	System.out.println("Invalid phone detected, setting fallback 9999999999. Received: " + userPhone);
            	userPhone = "9999999999";
            }
            
            String safeCustomerId = username.replaceAll("[^a-zA-Z0-9]", "_");
            Map<String, Object> customer = Map.of(
                    "customer_id", safeCustomerId,
                    "customer_name", userName,
                    "customer_email", userEmail,
                    "customer_phone", userPhone
            );

            Map<String, Object> body = Map.of(
                    "order_id", orderId,
                    "order_amount", amount,
                    "order_currency", currency.equalsIgnoreCase("USD") ? "INR" : currency.equalsIgnoreCase("EUR") ? "INR" : currency.equalsIgnoreCase("GBP") ? "INR" : currency.equalsIgnoreCase("JPY") ? "INR": currency,
                    "customer_details", customer,
                    "order_meta", Map.of(
                            "return_url", "http://localhost:5173/payment/success?order_id={order_id}&payment_id={payment_id}&status={order_status}",
                            "return_url_type", "GET",
                            "notify_url", "https://romaine-peperine-dotty.ngrok-free.dev/api/payment/webhook"
                            
                            
                    )
//                    "order_meta", Map.of(
//                    	    "return_url", "http://localhost:5173/payment/success?order_id={order_id}&payment_id={payment_id}&status={order_status}",
//                    	    "payment_failure_url", "http://localhost:5173/payment/failure",
//                    	    "notify_url", "https://romaine-peperine-dotty.ngrok-free.dev/api/payment/webhook",
//                    	    "payment_redirect", true
//                    	)
            );

            System.out.println("🧾 Sending Cashfree Order Request: " + body);
            

            return cashfreeClient.post()
                    .uri("/pg/orders")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .doOnNext(res -> System.out.println("✅ Cashfree Response: " + res))
                    .onErrorResume(WebClientResponseException.class, err -> {
                        System.err.println("❌ Cashfree API Error: " + err.getRawStatusCode() + " - " + err.getResponseBodyAsString());
                        Map<String, Object> errorMap = new HashMap<>();
                        errorMap.put("error", "Cashfree API error: " + err.getRawStatusCode());
                        errorMap.put("details", err.getResponseBodyAsString());
                        return Mono.just(errorMap);
                    });
        });
    }
}