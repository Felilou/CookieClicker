package at.spengergasse.gameservice.client;

import at.spengergasse.gameservice.dto.SaveGameRequest;
import at.spengergasse.gameservice.model.GameState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PersistenceServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${persistence-service.url}")
    private String persistenceServiceUrl;

    public void saveGame(GameState gameState) {
        List<SaveGameRequest.PlayerScore> players = gameState.getScores().entrySet().stream()
                .sorted(java.util.Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> new SaveGameRequest.PlayerScore(e.getKey(), e.getValue()))
                .toList();

        SaveGameRequest request = new SaveGameRequest(players, gameState.getWinner());

        webClientBuilder.build()
                .post()
                .uri(persistenceServiceUrl + "/api/stats/game")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        null,
                        err -> log.error("Failed to persist game for room {}: {}", gameState.getRoomId(), err.getMessage())
                );
    }

}
