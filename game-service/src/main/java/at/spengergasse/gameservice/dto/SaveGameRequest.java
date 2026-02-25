package at.spengergasse.gameservice.dto;

import java.util.List;

public record SaveGameRequest(
        List<PlayerScore> players,
        String winnerUsername
) {
    public record PlayerScore(String username, int score) {}
}
