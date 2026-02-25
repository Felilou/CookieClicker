package at.spengergasse.gameservice.service;

import at.spengergasse.gameservice.client.PersistenceServiceClient;
import at.spengergasse.gameservice.model.GameState;
import at.spengergasse.gameservice.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RoomService {

    private final Map<String, GameState> rooms = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    // Setter injection to avoid potential circular dependency with SimpMessagingTemplate
    private SimpMessagingTemplate messagingTemplate;
    private PersistenceServiceClient persistenceServiceClient;

    @Autowired
    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Autowired
    public void setPersistenceServiceClient(PersistenceServiceClient persistenceServiceClient) {
        this.persistenceServiceClient = persistenceServiceClient;
    }

    // ─── Public API ──────────────────────────────────────────────────────────────

    /**
     * Creates a new room and adds the creator as the first player.
     * If the room already exists the existing state is returned.
     */
    public GameState createRoom(String roomId, String creatorUsername) {
        rooms.putIfAbsent(roomId, new GameState(roomId, creatorUsername));
        GameState room = rooms.get(roomId);
        room.addPlayer(creatorUsername);
        return room;
    }

    /**
     * Handles an incoming message for an existing room.
     * Returns the updated GameState for synchronous actions (JOIN, LEAVE, CLICK).
     * Returns null for START_GAME because the service manages all broadcasts asynchronously.
     */
    public GameState handleMessage(String roomId, Message message) {
        GameState room = rooms.get(roomId);
        if (room == null) {
            log.warn("Message for unknown room: {}", roomId);
            return null;
        }

        switch (message.action()) {
            case JOIN -> room.addPlayer(message.username());
            case LEAVE -> room.removePlayer(message.username());
            case CLICK -> room.click(message.username());
            case START_GAME -> {
                if (room.canStart(message.username())) {
                    startCountdown(roomId, room);
                } else {
                    log.warn("User {} cannot start game in room {}", message.username(), roomId);
                }
                return null; // service broadcasts asynchronously
            }
        }

        return room;
    }

    public Map<String, GameState> getRooms() {
        return Collections.unmodifiableMap(rooms);
    }

    // ─── Countdown + Game timer ───────────────────────────────────────────────────

    private void startCountdown(String roomId, GameState room) {
        room.beginCountdown(); // state = COUNTDOWN, countdownValue = 3
        broadcast(roomId, room);

        // t+1s → countdown = 2
        scheduler.schedule(() -> {
            room.setCountdownValue(2);
            broadcast(roomId, room);
        }, 1, TimeUnit.SECONDS);

        // t+2s → countdown = 1
        scheduler.schedule(() -> {
            room.setCountdownValue(1);
            broadcast(roomId, room);
        }, 2, TimeUnit.SECONDS);

        // t+3s → IN_PROGRESS
        scheduler.schedule(() -> {
            long endsAt = System.currentTimeMillis() + 10_000L;
            room.startGame(endsAt);
            broadcast(roomId, room);
        }, 3, TimeUnit.SECONDS);

        // t+13s → ENDED
        scheduler.schedule(() -> {
            room.endGame();
            broadcast(roomId, room);
            log.info("Game ended in room {}. Winner: {}", roomId, room.getWinner());
            persistenceServiceClient.saveGame(room);
        }, 13, TimeUnit.SECONDS);
    }

    private void broadcast(String roomId, GameState state) {
        messagingTemplate.convertAndSend("/room/" + roomId, state);
    }

}
