package com.smarttrip.authservice.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
	
	private final Key key = Keys.hmacShaKeyFor("MyUltraLongSuperSecretKeyThatIsAtLeast32CharsLong!!".getBytes());
    private final long EXPIRATION_TIME = 86400000; // 24 hours

    // ✅ Generate JWT token
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)  // subject = email
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    // ✅ Validate JWT and return claims
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)   // use same key
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ Extract just email from token
    public String extractEmail(String token) {
        return validateToken(token).getSubject();
    }
 // ✅ Validate token and get Claims
    public Claims validateTokenAndGetClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)   // same key used to sign
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
//package com.smarttrip.authservice.util;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Date;
//
//@Component
//public class JwtUtil {
//
//    // 🔑 Shared constant key (keep same in TripService)
//    private static final String SECRET_KEY = "SMARTTRIP_SUPER_SECRET_KEY_2025_SMARTTRIP_APP";
//
//    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
//    private final long EXPIRATION_TIME = 86400000; // 24 hours
//
//    // ✅ Generate JWT token
//    public String generateToken(String email) {
//        return Jwts.builder()
//                .setSubject(email)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    // ✅ Extract email
//    public String extractEmail(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    // ✅ Validate token
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token);
//            return true;
//        } catch (JwtException e) {
//            return false;
//        }
//    }
//}