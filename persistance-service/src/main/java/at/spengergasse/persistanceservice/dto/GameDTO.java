package at.spengergasse.persistanceservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GameDTO(
        String winnerUsername,
        List<PlayerScore> players,
        LocalDateTime timestamp
) {
    public record PlayerScore(String username, int score) {}
}
