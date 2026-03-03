package at.spengergasse.commons.dto;

import at.spengergasse.commons.model.State;

public record RoomInfo(
        String roomId,
        String creatorUsername,
        int playerCount,
        State state
) {}

