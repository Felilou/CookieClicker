package at.spengergasse.gameservice.model;

import at.spengergasse.commons.model.State;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class GameState {

    private final String roomId;
    private final String creatorUsername;

    private State state = State.WAITING_FOR_PLAYERS;

    /** Players currently connected to the room. */
    private final Set<String> connectedPlayers = new LinkedHashSet<>();

    /** Players who were connected when IN_PROGRESS began — they can click and rejoin. */
    private final Set<String> registeredPlayers = new LinkedHashSet<>();

    /** Click counts, populated at game start for every registered player. */
    private final Map<String, Integer> scores = new LinkedHashMap<>();

    /** Players who joined after game start (spectators). */
    private final Set<String> spectators = new LinkedHashSet<>();

    private String winner;
    private int countdownValue;
    private long gameEndsAt; // epoch millis

    public GameState(String roomId, String creatorUsername) {
        this.roomId = roomId;
        this.creatorUsername = creatorUsername;
    }

    /**
     * Adds a player to the room:
     * - WAITING / COUNTDOWN: joins as regular player
     * - IN_PROGRESS: registered players rejoin, others become spectators
     * - ENDED: spectator only
     */
    public void addPlayer(String username) {
        if (connectedPlayers.contains(username) || spectators.contains(username)) return;

        switch (state) {
            case WAITING_FOR_PLAYERS, COUNTDOWN -> connectedPlayers.add(username);
            case IN_PROGRESS -> {
                if (registeredPlayers.contains(username)) {
                    connectedPlayers.add(username); // rejoin during game
                } else {
                    spectators.add(username);
                }
            }
            case ENDED -> spectators.add(username);
        }
    }

    public void removePlayer(String username) {
        connectedPlayers.remove(username);
        spectators.remove(username);
    }

    /** Called at the start of the 3-second countdown. */
    public void beginCountdown() {
        state = State.COUNTDOWN;
        countdownValue = 3;
    }

    public void setCountdownValue(int value) {
        this.countdownValue = value;
    }

    /** Called when IN_PROGRESS phase begins. Snapshots connected players as registered. */
    public void startGame(long endsAt) {
        registeredPlayers.addAll(connectedPlayers);
        registeredPlayers.forEach(p -> scores.put(p, 0));
        state = State.IN_PROGRESS;
        gameEndsAt = endsAt;
    }

    /** Records a click for a player (only if they are registered and currently connected). */
    public void click(String username) {
        if (state == State.IN_PROGRESS
                && connectedPlayers.contains(username)
                && registeredPlayers.contains(username)) {
            scores.merge(username, 1, Integer::sum);
        }
    }

    /** Ends the game and determines the winner. */
    public void endGame() {
        state = State.ENDED;
        winner = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /** Returns true if the given user can start the game. */
    public boolean canStart(String username) {
        return username.equals(creatorUsername)
                && state == State.WAITING_FOR_PLAYERS
                && connectedPlayers.size() >= 2;
    }

}
