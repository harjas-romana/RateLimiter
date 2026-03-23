package com.ratelimiter.util;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtil {
    
    //In tesing, i am hardcoding the key here, but, in production it will come from application properties or environment variable

    private final String SECRET_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyXzEyMyJ9.2kh4grAqqDRCL7w-TZMBO_S7oRTJenwG2_z7RRFBA_4";

    public String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    
    // Log exactly what is being received to the server console
    System.out.println("DEBUG: Raw Auth Header -> [" + bearerToken + "]");

    if (bearerToken != null && !bearerToken.isEmpty()) {
        // Trim to remove any accidental trailing spaces from the curl command
        bearerToken = bearerToken.trim();
        
        if (bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            System.out.println("DEBUG: Extracted Token -> [" + token + "]");
            return token;
        } else {
            System.out.println("DEBUG: Header did not start with 'Bearer '");
        }
    } else {
        System.out.println("DEBUG: Auth Header was null or empty");
    }
    return null; 
}

    public String extractUserId(String token) {

        try {
            
            Claims claims = Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes()).build().parseClaimsJws(token).getBody();
            return claims.getSubject(); //subject is user ID
        } catch (Exception e) {
            System.out.println("Invalid JWT Token: " + e.getMessage());
            return null;
        }
    }
}
