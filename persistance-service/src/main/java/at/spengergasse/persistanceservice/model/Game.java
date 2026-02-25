package at.spengergasse.persistanceservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Builder
public class Game extends AbstractPersistable<Long> {

    private String winnerUsername;
    private LocalDateTime timestamp;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_player_results", joinColumns = @JoinColumn(name = "game_id"))
    private List<PlayerResult> playerResults;

}
