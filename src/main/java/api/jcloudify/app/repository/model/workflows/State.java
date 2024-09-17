package api.jcloudify.app.repository.model.workflows;

import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;

import java.io.Serializable;
import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public abstract class State<T extends Enum<T>> implements Serializable {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @JdbcTypeCode(NAMED_ENUM)
    @Enumerated(STRING)
    private T progressionStatus;

    private Instant timestamp;

    @JdbcTypeCode(NAMED_ENUM)
    @Enumerated(STRING)
    private ExecutionType executionType;

    public enum ExecutionType{
        ASYNCHRONOUS, SYNCHRONOUS
    }
}
