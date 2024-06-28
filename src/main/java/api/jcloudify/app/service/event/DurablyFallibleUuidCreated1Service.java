package api.jcloudify.app.service.event;

import static java.lang.Thread.sleep;

import api.jcloudify.app.PojaGenerated;
import api.jcloudify.app.endpoint.event.model.DurablyFallibleUuidCreated1;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@PojaGenerated
@Service
@AllArgsConstructor
@Slf4j
public class DurablyFallibleUuidCreated1Service implements Consumer<DurablyFallibleUuidCreated1> {
  private final UuidCreatedService uuidCreatedService;

  @SneakyThrows
  @Override
  public void accept(DurablyFallibleUuidCreated1 durablyFallibleUuidCreated1) {
    sleep(durablyFallibleUuidCreated1.getWaitDurationBeforeConsumingInSeconds() * 1_000L);
    if (durablyFallibleUuidCreated1.shouldFail()) {
      throw new RuntimeException("Oops, random fail!");
    }

    uuidCreatedService.accept(durablyFallibleUuidCreated1.getUuidCreated());
  }
}
