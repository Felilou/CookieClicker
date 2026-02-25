package at.spengergasse.persistanceservice.service;

import at.spengergasse.persistanceservice.client.AuthServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final AuthServiceClient authServiceClient;

    /**
     * Extracts username from JWT token in SecurityContext
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            // Extract username from JWT claims
            String username = jwt.getClaim("preferred_username");
            log.info("Extracted username from JWT: {}", username);
            return username;
        }

        throw new RuntimeException("No JWT authentication found");
    }

    /**
     * Alternative method: Uses Feign Client to call auth-service
     * This is useful when you want to validate the token or get additional user info
     */
    public String getUsernameViaAuthService() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String token = jwtAuth.getToken().getTokenValue();
            Map<String, String> response = authServiceClient.getUsername("Bearer " + token);
            String username = response.get("username");
            log.info("Fetched username via auth-service: {}", username);
            return username;
        }

        throw new RuntimeException("No JWT authentication found");
    }

    /**
     * Validates token via auth-service
     */
    public boolean validateToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken().getTokenValue();
                Map<String, String> response = authServiceClient.validateToken("Bearer " + token);
                return "true".equals(response.get("valid"));
            }

            return false;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
}

