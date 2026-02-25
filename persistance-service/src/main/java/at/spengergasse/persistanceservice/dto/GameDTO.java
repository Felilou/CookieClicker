package at.spengergasse.persistanceservice.dto;

import java.time.LocalDateTime;

public record GameDTO(
        String player1Name,
        String player2Name,
        int player1Score,
        int player2Score,
        LocalDateTime timestamp
) {
}
