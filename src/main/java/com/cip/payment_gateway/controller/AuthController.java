package com.cip.payment_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.cip.payment_gateway.config.JwtUtil;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtUtil jwtUtil;

    // Dummy Endpoint for Token
    @GetMapping("/token")
    public ResponseEntity<?> generateToken() {
        // Langsung generate token tanpa login
        String token = jwtUtil.generateToken("api-client");
        return ResponseEntity.ok(token);
    }
}
