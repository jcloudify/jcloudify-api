package api.jcloudify.app.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
public class State<T extends Enum<T>> {
    private final T state;
    @Getter private final Instant t;

    private final ExecutionType executionType;

    public enum ExecutionType{
        ASYNCHRONOUS, SYNCHRONOUS
    }
}

