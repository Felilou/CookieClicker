package at.spengergasse.persistanceservice.repository;

import at.spengergasse.persistanceservice.dto.GameDTO;
import at.spengergasse.persistanceservice.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("select new at.spengergasse.persistanceservice.dto.GameDTO(g.player1Name, g.player2Name, g.player1Score, g.player2Score, g.timestamp) " +
           "from Game g where g.player1Name = :playerName or g.player2Name = :playerName")
    List<GameDTO> findByPlayerName(@Param("playerName") String playerName);
}
