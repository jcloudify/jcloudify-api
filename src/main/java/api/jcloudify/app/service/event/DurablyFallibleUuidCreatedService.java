package api.jcloudify.app.service.event;

import static java.lang.Thread.sleep;

import api.jcloudify.app.PojaGenerated;
import api.jcloudify.app.endpoint.event.model.DurablyFallibleUuidCreated;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@PojaGenerated
@Service
@AllArgsConstructor
@Slf4j
public class DurablyFallibleUuidCreatedService implements Consumer<DurablyFallibleUuidCreated> {
  private final UuidCreatedService uuidCreatedService;

  @SneakyThrows
  @Override
  public void accept(DurablyFallibleUuidCreated durablyFallibleUuidCreated) {
    sleep(durablyFallibleUuidCreated.getWaitDurationBeforeConsumingInSeconds() * 1_000L);
    if (durablyFallibleUuidCreated.shouldFail()) {
      throw new RuntimeException("Oops, random fail!");
    }

    uuidCreatedService.accept(durablyFallibleUuidCreated.getUuidCreated());
  }
}
