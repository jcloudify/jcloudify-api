package api.jcloudify.app.endpoint.rest.controller.health;

import static api.jcloudify.app.endpoint.rest.controller.health.PingController.KO;
import static api.jcloudify.app.endpoint.rest.controller.health.PingController.OK;
import static java.util.UUID.randomUUID;

import api.jcloudify.app.PojaGenerated;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.DurablyFallibleUuidCreated1;
import api.jcloudify.app.endpoint.event.model.DurablyFallibleUuidCreated2;
import api.jcloudify.app.endpoint.event.model.UuidCreated;
import api.jcloudify.app.repository.DummyUuidRepository;
import api.jcloudify.app.repository.model.DummyUuid;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@PojaGenerated
@RestController
@AllArgsConstructor
public class HealthEventController {

  private final DummyUuidRepository dummyUuidRepository;
  private final EventProducer eventProducer;

  @GetMapping(value = "/health/event1")
  public List<String> handleEvent1(
      @RequestParam(defaultValue = "1") int nbEvent,
      @RequestParam(defaultValue = "2") int waitInSeconds) {
    return handleEvent(nbEvent, waitInSeconds, DurablyFallibleUuidCreated1.class);
  }

  @GetMapping(value = "/health/event2")
  public List<String> handleEvent2(
      @RequestParam(defaultValue = "1") int nbEvent,
      @RequestParam(defaultValue = "2") int waitInSeconds) {
    return handleEvent(nbEvent, waitInSeconds, DurablyFallibleUuidCreated2.class);
  }

  @PostMapping(value = "/health/event/uuids")
  public ResponseEntity<String> checkUuids(@RequestBody List<String> uuids) {
    return checkUuidsSaved(uuids) ? OK : KO;
  }

  private <T> List<String> handleEvent(int nbEvent, int waitInSeconds, Class<T> eventType) {
    validateNbEvent(nbEvent);

    List<String> uuids = generateUuids(nbEvent);
    fireEvents(uuids, waitInSeconds, eventType);

    return uuids;
  }

  private void validateNbEvent(int nbEvent) {
    if (nbEvent < 1 || nbEvent > 500) {
      throw new IllegalArgumentException("nbEvent must be between 1 and 500");
    }
  }

  private List<String> generateUuids(int nbEvent) {
    List<String> uuids = new ArrayList<>();
    for (int i = 0; i < nbEvent; i++) {
      uuids.add(randomUUID().toString());
    }
    return uuids;
  }

  private <T> void fireEvents(List<String> uuids, int waitInSeconds, Class<T> eventType) {
    eventProducer.accept(
        uuids.stream().map(uuid -> createEvent(uuid, waitInSeconds, eventType)).toList());
  }

  private <T> T createEvent(String uuid, int waitInSeconds, Class<T> eventType) {
    UuidCreated uuidCreated = new UuidCreated(uuid);
    double failureRate = 0.1;

    if (eventType.equals(DurablyFallibleUuidCreated1.class)) {
      return eventType.cast(
          DurablyFallibleUuidCreated1.builder()
              .uuidCreated(uuidCreated)
              .failureRate(failureRate)
              .waitDurationBeforeConsumingInSeconds(waitInSeconds)
              .build());
    } else if (eventType.equals(DurablyFallibleUuidCreated2.class)) {
      return eventType.cast(
          DurablyFallibleUuidCreated2.builder()
              .uuidCreated(uuidCreated)
              .failureRate(failureRate)
              .waitDurationBeforeConsumingInSeconds(waitInSeconds)
              .build());
    } else {
      throw new IllegalArgumentException("Unknown event type: " + eventType);
    }
  }

  private boolean checkUuidsSaved(List<String> uuids) {
    List<String> savedUuids =
        dummyUuidRepository.findAllById(uuids).stream().map(DummyUuid::getId).toList();
    return savedUuids.containsAll(uuids);
  }
}
