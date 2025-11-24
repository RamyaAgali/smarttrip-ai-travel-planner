package com.smarttrip.authservice.controller;

import com.smarttrip.authservice.dto.AuthResponse;
import com.smarttrip.authservice.model.AppUser;
import com.smarttrip.authservice.repository.UserRepository;
import com.smarttrip.authservice.service.EmailService;
import com.smarttrip.authservice.service.UserService;
import com.smarttrip.authservice.util.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public AuthController(UserService userService, JwtUtil jwtUtil,EmailService emailService, UserRepository userRepository) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody AppUser user) {
        try {
            AppUser savedUser = userService.registerUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        return userService.loginUser(email, password)
                .<ResponseEntity<?>>map(user -> {
                    String token = jwtUtil.generateToken(email);

                    // ✅ Directly use user.getEmail() and user.getName()
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "user", Map.of(
                                    "email", user.getEmail(),
                                    "name", user.getName()
                            )
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);

            return userService.findByEmail(email)
                    .map(user -> ResponseEntity.ok(Map.of(
                            "name", user.getName(),
                            "email", user.getEmail()
                    )))
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(Map.of("error", "User not found")));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        Optional<AppUser> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        // ✅ Use UserService to create and save token
        String resetLink = userService.createPasswordResetToken(email);

        // ✅ Send email with the stored token
        emailService.sendPasswordResetEmail(email, resetLink);

        return ResponseEntity.ok("Password reset link sent to " + email);
    }



    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
        
        
    }
    @GetMapping("/{email}")
    public ResponseEntity<AppUser> getUserByEmail(
            @PathVariable String email,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // ✅ Security check
        if (authHeader == null || !authHeader.equals("Bearer SMARTTRIP_INTERNAL_SECRET_2025")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserByUsername(
            @PathVariable String username,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
    	if (authHeader == null || !authHeader.equals("Bearer SMARTTRIP_INTERNAL_SECRET_2025")) {
    	    return ResponseEntity.status(HttpStatus.FORBIDDEN)
    	            .body(Map.of("error", "Unauthorized internal request"));
    	}

    	// ✅ Convert encoded username safely back to real email
    	String actualEmail = username.replace("_", ".").replace("_gmail_com", "@gmail.com");

    	System.out.println("🔍 Looking up user by email: " + actualEmail);

    	return userRepository.findByEmail(actualEmail)
    	        .map(user -> ResponseEntity.ok(Map.of(
    	                "name", user.getName(),
    	                "email", user.getEmail(),
    	                "mobileNumber", user.getMobile() != null ? user.getMobile() : "9999999999"
    	        )))
    	        .orElse(ResponseEntity.status(404)
    	                .body(Map.of("error", "User not found for: " + actualEmail)));
    }

}
