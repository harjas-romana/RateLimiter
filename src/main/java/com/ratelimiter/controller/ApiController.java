package com.ratelimiter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/public")
    public ResponseEntity<String> publicEndPoint() {
        return ResponseEntity.ok("No rate limit here. Public Ropute");
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndPoint() {
        return ResponseEntity.ok("You passed the rate limiter bruh!");
    }
}
