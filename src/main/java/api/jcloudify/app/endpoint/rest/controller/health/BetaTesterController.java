package api.jcloudify.app.endpoint.rest.controller.health;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BetaTesterController {
  @GetMapping(value = "/beta-ping", produces = TEXT_PLAIN)
  public ResponseEntity<String> ping() {
    return ok("beta-pong");
  }
}
