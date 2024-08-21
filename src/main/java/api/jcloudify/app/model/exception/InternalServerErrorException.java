package api.jcloudify.app.model.exception;

public class InternalServerErrorException extends ApiException {
  public InternalServerErrorException(Exception source) {
    super(ExceptionType.SERVER_EXCEPTION, source);
  }

  public InternalServerErrorException(String message) {
    super(ExceptionType.SERVER_EXCEPTION, message);
  }
}
