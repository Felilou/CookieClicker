package at.spengergasse.persistanceservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    public Map<String, String> getUsername(String bearerToken) {
        return webClientBuilder.build()
                .get()
                .uri(authServiceUrl + "/auth/username")
                .header("Authorization", bearerToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();
    }

    public Map<String, String> validateToken(String bearerToken) {
        return webClientBuilder.build()
                .get()
                .uri(authServiceUrl + "/auth/validate")
                .header("Authorization", bearerToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();
    }
}
