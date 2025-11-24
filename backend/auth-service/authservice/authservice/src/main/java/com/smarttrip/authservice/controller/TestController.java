package com.smarttrip.authservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/secure")
    public ResponseEntity<String> secureTest(HttpServletRequest request) {
        String userEmail = (String) request.getAttribute("userEmail");
        return ResponseEntity.ok("✅ Protected route accessed by: " + userEmail);
    }
}
