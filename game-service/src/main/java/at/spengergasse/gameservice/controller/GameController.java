package at.spengergasse.gameservice.controller;

import at.spengergasse.gameservice.model.GameState;
import at.spengergasse.gameservice.model.Message;
import at.spengergasse.gameservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles all in-room actions (JOIN, LEAVE, CLICK, START_GAME).
     * For START_GAME, RoomService manages async broadcasts and returns null here.
     */
    @MessageMapping("/room/{gameID}")
    public void handleGameMessage(@DestinationVariable String gameID, Message message) {
        GameState state = roomService.handleMessage(gameID, message);
        if (state != null) {
            messagingTemplate.convertAndSend("/room/" + gameID, state);
        }
    }

    /**
     * Creates a new room. The message body must contain the creator's username.
     */
    @MessageMapping("/createRoom/{roomID}")
    public void createRoom(@DestinationVariable String roomID, Message message) {
        GameState state = roomService.createRoom(roomID, message.username());
        messagingTemplate.convertAndSend("/room/" + roomID, state);
    }

}
