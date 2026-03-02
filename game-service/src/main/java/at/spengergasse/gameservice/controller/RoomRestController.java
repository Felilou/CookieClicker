package at.spengergasse.gameservice.controller;

import at.spengergasse.gameservice.dto.RoomInfo;
import at.spengergasse.gameservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomRestController {

    private final RoomService roomService;

    @GetMapping
    public List<RoomInfo> getRooms() {
        return roomService.getRooms().entrySet().stream()
                .map(e -> new RoomInfo(
                        e.getKey(),
                        e.getValue().getCreatorUsername(),
                        e.getValue().getConnectedPlayers().size(),
                        e.getValue().getState()
                ))
                .toList();
    }

}
