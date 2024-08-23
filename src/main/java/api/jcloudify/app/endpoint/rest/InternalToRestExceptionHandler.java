package api.jcloudify.app.endpoint.rest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import api.jcloudify.app.endpoint.rest.model.ExceptionModel;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.model.exception.ForbiddenException;
import api.jcloudify.app.model.exception.NotFoundException;
import java.nio.file.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class InternalToRestExceptionHandler {
  @ExceptionHandler(value = {BadRequestException.class})
  ResponseEntity<ExceptionModel> handleBadRequest(BadRequestException e) {
    log.info("Bad request", e);
    return new ResponseEntity<>(toRest(e, BAD_REQUEST), BAD_REQUEST);
  }

  @ExceptionHandler(value = {NotFoundException.class})
  ResponseEntity<ExceptionModel> handleNotFound(NotFoundException e) {
    log.info("Not found", e);
    return new ResponseEntity<>(toRest(e, NOT_FOUND), NOT_FOUND);
  }

  @ExceptionHandler(value = {java.lang.Exception.class})
  ResponseEntity<ExceptionModel> handleDefault(java.lang.Exception e) {
    log.error("Internal error", e);
    return new ResponseEntity<>(toRest(e, INTERNAL_SERVER_ERROR), INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(
      value = {
        AccessDeniedException.class,
        ForbiddenException.class,
        AuthenticationException.class,
        UsernameNotFoundException.class
      })
  ResponseEntity<ExceptionModel> handleForbidden(java.lang.Exception e) {
    /* rest.model.Exception.Type.FORBIDDEN designates both authentication and authorization errors.
     * Hence do _not_ HttpsStatus.UNAUTHORIZED because, counter-intuitively,
     * it's just for authentication.
     * https://stackoverflow.com/questions/3297048/403-forbidden-vs-401-unauthorized-http-responses */
    log.info("Forbidden", e);
    var restException = new ExceptionModel();
    restException.setType(FORBIDDEN.toString());
    restException.setMessage(e.getMessage());
    return new ResponseEntity<>(restException, FORBIDDEN);
  }

  @ExceptionHandler(value = {MissingServletRequestParameterException.class})
  ResponseEntity<ExceptionModel> handleDataIntegrityViolation(
      MissingServletRequestParameterException e) {
    log.info("Missing parameter", e);
    return new ResponseEntity<>(toRest(e, BAD_REQUEST), BAD_REQUEST);
  }

  @ExceptionHandler(value = {DataIntegrityViolationException.class})
  ResponseEntity<ExceptionModel> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    log.info("Bad request", e);
    return new ResponseEntity<>(toRest(e, BAD_REQUEST), BAD_REQUEST);
  }

  private ExceptionModel toRest(java.lang.Exception e, HttpStatus httpStatus) {
    var restException = new ExceptionModel();
    restException.setType(httpStatus.toString());
    restException.setMessage(e.getMessage());
    return restException;
  }
}
