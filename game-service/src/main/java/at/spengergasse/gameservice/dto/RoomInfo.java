package at.spengergasse.gameservice.dto;

import at.spengergasse.gameservice.model.State;

public record RoomInfo(
        String roomId,
        String creatorUsername,
        int playerCount,
        State state
) {}
