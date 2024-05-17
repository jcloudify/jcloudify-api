package api.jcloudify.app.model.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
  private final ExceptionType type;

  public ApiException(ExceptionType type, String message) {
    super(message);
    this.type = type;
  }

  public ApiException(ExceptionType type, Exception source) {
    super(source);
    this.type = type;
  }

  public enum ExceptionType {
    CLIENT_EXCEPTION,
    SERVER_EXCEPTION
  }
}
