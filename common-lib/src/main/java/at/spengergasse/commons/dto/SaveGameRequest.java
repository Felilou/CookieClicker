package at.spengergasse.commons.dto;

import java.util.List;

public record SaveGameRequest(
        List<PlayerScore> players,
        String winnerUsername
) {}

