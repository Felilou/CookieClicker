package at.spengergasse.persistanceservice.dto;

public record PersistGameRequest(
        String player1Name,
        String player2Name,
        int player1Score,
        int player2Score
) {
}
