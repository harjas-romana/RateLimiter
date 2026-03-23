package com.ratelimiter.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ratelimiter.service.RateLimiterService; // <-- This was the main culprit
import com.ratelimiter.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }
        
    String authHeader = request.getHeader("Authorization");
    System.out.println("DEBUG: Received Authorization Header: " + authHeader); // <--- ADD THIS
        
        String token = jwtUtil.extractToken(request);
        
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization header");
            return false;
        }

         String userId = jwtUtil.extractUserId(token);
        
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid Token");
            return false;
        }

        // The core check
        boolean isAllowed = rateLimiterService.isAllowed(userId);
        
        if (!isAllowed) {
            response.setStatus(429); // 429 Too Many Requests
            response.getWriter().write("Rate limit exceeded. Try again later.");
            return false;
        }

        return true; // Let the request pass to the controller
    }
}