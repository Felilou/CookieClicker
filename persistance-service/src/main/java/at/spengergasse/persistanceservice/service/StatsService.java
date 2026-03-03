package at.spengergasse.persistanceservice.service;

import at.spengergasse.commons.dto.GameDTO;
import at.spengergasse.commons.dto.PlayerScore;
import at.spengergasse.commons.dto.SaveGameRequest;
import at.spengergasse.persistanceservice.model.Game;
import at.spengergasse.persistanceservice.model.PlayerResult;
import at.spengergasse.persistanceservice.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final GameRepository gameRepository;

    public long getTotalGames() {
        long count = gameRepository.count();
        log.info("Total games in database: {}", count);
        return count;
    }

    public List<GameDTO> getGamesByUser(String username) {
        return gameRepository.findByPlayerName(username).stream()
                .map(this::toDTO)
                .toList();
    }

    public Game addGame(SaveGameRequest request) {
        log.info("Adding game, winner: {}, players: {}", request.winnerUsername(), request.players().size());

        List<PlayerResult> results = request.players().stream()
                .map(p -> new PlayerResult(p.username(), p.score()))
                .toList();

        Game game = Game.builder()
                .winnerUsername(request.winnerUsername())
                .playerResults(results)
                .timestamp(LocalDateTime.now())
                .build();

        return gameRepository.save(game);
    }

    private GameDTO toDTO(Game game) {
        List<PlayerScore> players = game.getPlayerResults().stream()
                .map(pr -> new PlayerScore(pr.getUsername(), pr.getScore()))
                .toList();
        return new GameDTO(game.getWinnerUsername(), players, game.getTimestamp());
    }

}
