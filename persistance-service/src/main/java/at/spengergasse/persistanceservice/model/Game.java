package at.spengergasse.persistanceservice.model;

import jakarta.persistence.Entity;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Builder
public class Game extends AbstractPersistable<Long> {

    private final String player1Name;
    private final String player2Name;
    private final int player1Score;
    private final int player2Score;
    private final LocalDateTime timestamp;

}
