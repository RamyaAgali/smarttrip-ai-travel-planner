package com.smarttrip.tripservice;

import java.net.URL;
import java.net.HttpURLConnection;

public class TestNetwork {
    public static void main(String[] args) {
        try {
            URL url = new URL("https://api.geoapify.com/v1/geocode/search?text=Goa&apiKey=d879b42a725744c9bac39835f4bc5da6");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.connect();
            System.out.println("✅ Connected successfully: " + conn.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}