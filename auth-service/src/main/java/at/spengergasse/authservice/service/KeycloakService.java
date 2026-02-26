package at.spengergasse.authservice.service;

import at.spengergasse.authservice.dto.LoginRequest;
import at.spengergasse.authservice.dto.RegisterRequest;
import at.spengergasse.authservice.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final WebClient.Builder webClientBuilder;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-username:admin}")
    private String adminUsername;

    @Value("${keycloak.admin-password:admin}")
    private String adminPassword;

    public TokenResponse login(LoginRequest loginRequest) {
        log.info("Attempting to login user: {}", loginRequest.getUsername());

        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                authServerUrl, realm);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", "password");
        formData.add("scope", "openid");
        formData.add("username", loginRequest.getUsername());
        formData.add("password", loginRequest.getPassword());

        try {
            TokenResponse response = webClientBuilder.build()
                    .post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            log.info("Login successful for user: {}", loginRequest.getUsername());
            return response;
        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);
            throw new RuntimeException("Login failed: " + e.getMessage());
        }
    }

    public String getUsernameFromToken(String token) {
        log.info("Extracting username from token");

        String userInfoUrl = String.format("%s/realms/%s/protocol/openid-connect/userinfo",
                authServerUrl, realm);

        try {
            var userInfo = webClientBuilder.build()
                    .get()
                    .uri(userInfoUrl)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(java.util.Map.class)
                    .block();

            String username = (String) userInfo.get("preferred_username");
            log.info("Extracted username: {}", username);
            return username;
        } catch (Exception e) {
            log.error("Failed to extract username from token", e);
            throw new RuntimeException("Invalid token: " + e.getMessage());
        }
    }

    public String registerUser(RegisterRequest registerRequest) {
        log.info("Attempting to register user: {}", registerRequest.getUsername());

        try {
            String adminToken = getAdminAccessToken();

            String usersUrl = String.format("%s/admin/realms/%s/users", authServerUrl, realm);

            Map<String, Object> userRepresentation = Map.of(
                    "username", registerRequest.getUsername(),
                    "email", registerRequest.getEmail(),
                    "firstName", registerRequest.getFirstName() != null ? registerRequest.getFirstName() : "",
                    "lastName", registerRequest.getLastName() != null ? registerRequest.getLastName() : "",
                    "enabled", true,
                    "emailVerified", true,
                    "credentials", List.of(Map.of(
                            "type", "password",
                            "value", registerRequest.getPassword(),
                            "temporary", false
                    ))
            );

            try {
                webClientBuilder.build()
                        .post()
                        .uri(usersUrl)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(userRepresentation)
                        .retrieve()
                        .toBodilessEntity()
                        .block();

                log.info("User registered successfully: {}", registerRequest.getUsername());
                return registerRequest.getUsername();
            } catch (WebClientResponseException e) {
                if (e.getStatusCode() == HttpStatus.CONFLICT) {
                    log.error("User already exists: {}", registerRequest.getUsername());
                    throw new RuntimeException("User already exists: " + registerRequest.getUsername());
                }
                log.error("Failed to register user: {}", e.getResponseBodyAsString(), e);
                throw new RuntimeException("Failed to register user: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("User registration failed for user: {}", registerRequest.getUsername(), e);
            throw new RuntimeException("User registration failed: " + e.getMessage());
        }
    }

    private String getAdminAccessToken() {
        log.info("Getting admin access token");

        String tokenUrl = String.format("%s/realms/master/protocol/openid-connect/token", authServerUrl);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", "admin-cli");
        formData.add("grant_type", "password");
        formData.add("username", adminUsername);
        formData.add("password", adminPassword);

        try {
            TokenResponse response = webClientBuilder.build()
                    .post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            log.info("Admin access token obtained successfully");
            return response.getAccessToken();
        } catch (Exception e) {
            log.error("Failed to get admin access token", e);
            throw new RuntimeException("Failed to get admin access token: " + e.getMessage());
        }
    }
}
