package at.spengergasse.persistanceservice.repository;

import at.spengergasse.persistanceservice.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("SELECT DISTINCT g FROM Game g JOIN g.playerResults pr WHERE pr.username = :username")
    List<Game> findByPlayerName(@Param("username") String username);

}
