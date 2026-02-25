package at.spengergasse.persistanceservice.service;

import at.spengergasse.persistanceservice.dto.GameDTO;
import at.spengergasse.persistanceservice.dto.PersistGameRequest;
import at.spengergasse.persistanceservice.model.Game;
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
        return gameRepository.findByPlayerName(username);
    }

    public Game addGame(PersistGameRequest persistGameRequest) {
        log.info("Adding game: {}", persistGameRequest);

        Game game = Game.builder()
                .player1Name(persistGameRequest.player1Name())
                .player2Name(persistGameRequest.player2Name())
                .player1Score(persistGameRequest.player1Score())
                .player2Score(persistGameRequest.player2Score())
                .timestamp(LocalDateTime.now())
                .build();

        return gameRepository.save(game);
    }
}

