package at.spengergasse.authservice.controller;

import at.spengergasse.authservice.dto.LoginRequest;
import at.spengergasse.authservice.dto.RegisterRequest;
import at.spengergasse.authservice.dto.RegisterResponse;
import at.spengergasse.authservice.dto.TokenResponse;
import at.spengergasse.authservice.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final KeycloakService keycloakService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        log.info("Registration request received for user: {}", registerRequest.getUsername());
        try {
            String username = keycloakService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse(null, username, "User registered successfully"));
        } catch (RuntimeException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegisterResponse(null, registerRequest.getUsername(), "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUsername());
        TokenResponse tokenResponse = keycloakService.login(loginRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody Map<String, String> body) {
        log.info("Token refresh request received");
        try {
            TokenResponse tokenResponse = keycloakService.refreshToken(body.get("refresh_token"));
            return ResponseEntity.ok(tokenResponse);
        } catch (RuntimeException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/username")
    public ResponseEntity<Map<String, String>> getUsername(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Username extraction request received");
        String token = authHeader.replace("Bearer ", "");
        String username = keycloakService.getUsernameFromToken(token);
        return ResponseEntity.ok(Map.of("username", username));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, String>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        log.info("Token validation request received");
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = keycloakService.getUsernameFromToken(token);
            return ResponseEntity.ok(Map.of("valid", "true", "username", username));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", "false"));
        }
    }
}
