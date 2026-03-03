package at.spengergasse.commons.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GameDTO(
        String winnerUsername,
        List<PlayerScore> players,
        LocalDateTime timestamp
) {}

