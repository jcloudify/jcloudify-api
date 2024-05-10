package api.poja.app.endpoint.rest.controller.health;

import static api.poja.app.endpoint.rest.controller.health.PingController.KO;
import static api.poja.app.endpoint.rest.controller.health.PingController.OK;

import api.poja.app.PojaGenerated;
import api.poja.app.repository.DummyRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@PojaGenerated
@RestController
@AllArgsConstructor
public class HealthDbController {

  DummyRepository dummyRepository;

  @GetMapping("/health/db")
  public ResponseEntity<String> dummyTable_should_not_be_empty() {
    return dummyRepository.findAll().isEmpty() ? KO : OK;
  }
}
