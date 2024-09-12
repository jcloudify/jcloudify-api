package api.jcloudify.app.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class State<T extends Enum<T>> {
  private final T state;
  @Getter private final Instant t;

  private final ExecutionType executionType;

  public enum ExecutionType {
    ASYNCHRONOUS,
    SYNCHRONOUS
  }
}
