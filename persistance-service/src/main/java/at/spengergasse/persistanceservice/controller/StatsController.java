package at.spengergasse.persistanceservice.controller;

import at.spengergasse.persistanceservice.client.AuthServiceClient;
import at.spengergasse.commons.dto.GameDTO;
import at.spengergasse.commons.dto.SaveGameRequest;
import at.spengergasse.commons.dto.StatsResponse;
import at.spengergasse.persistanceservice.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;
    private final AuthServiceClient authServiceClient;

    @GetMapping
    public ResponseEntity<StatsResponse> getStats() {
        log.info("Stats request received");

        long totalGames = statsService.getTotalGames();
        StatsResponse response = new StatsResponse(totalGames);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/game")
    public ResponseEntity<Void> addGame(@RequestBody SaveGameRequest request) {
        log.info("Add game request received: {}", request);

        long id = statsService.addGame(request).getId();

        return ResponseEntity.created(URI.create("/api/stats/game?id="+id)).build();
    }

    @GetMapping("/my-games")
    public ResponseEntity<List<GameDTO>> getMyGames(@RequestHeader("Authorization") String authHeader) {
        log.info("Get my games request received");

        String username = authServiceClient.getUsername(authHeader).get("username");
        List<GameDTO> games = statsService.getGamesByUser(username);

        return ResponseEntity.ok(games);
    }

}
