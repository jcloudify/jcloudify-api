package api.jcloudify.app.endpoint.rest.controller.health;

import static api.jcloudify.app.endpoint.rest.controller.health.PingController.KO;
import static api.jcloudify.app.endpoint.rest.controller.health.PingController.OK;
import static java.util.UUID.randomUUID;

import api.jcloudify.app.PojaGenerated;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.gen.UuidCreated;
import api.jcloudify.app.repository.DummyUuidRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@PojaGenerated
@RestController
@AllArgsConstructor
public class HealthEventController {

  DummyUuidRepository dummyUuidRepository;
  EventProducer eventProducer;

  @GetMapping(value = "/health/event")
  public ResponseEntity<String> random_uuid_is_fired_then_created() throws InterruptedException {
    var randomUuid = randomUUID().toString();
    var event = new UuidCreated().toBuilder().uuid(randomUuid).build();

    eventProducer.accept(List.of(event));

    Thread.sleep(20_000);
    return dummyUuidRepository.findById(randomUuid).map(dummyUuid -> OK).orElse(KO);
  }
}
