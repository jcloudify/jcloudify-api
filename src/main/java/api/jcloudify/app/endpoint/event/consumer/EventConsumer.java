package api.jcloudify.app.endpoint.event.consumer;

import static java.util.stream.Collectors.toList;

import api.jcloudify.app.PojaGenerated;
import api.jcloudify.app.concurrency.Workers;
import api.jcloudify.app.endpoint.event.consumer.model.ConsumableEvent;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@PojaGenerated
@Component
@Slf4j
public class EventConsumer implements Consumer<List<ConsumableEvent>> {
  private final Workers workers;
  private final EventServiceInvoker eventServiceInvoker;

  public EventConsumer(Workers workers, EventServiceInvoker eventServiceInvoker) {
    this.workers = workers;
    this.eventServiceInvoker = eventServiceInvoker;
  }

  @Override
  public void accept(List<ConsumableEvent> consumableEvents) {
    workers.invokeAll(consumableEvents.stream().map(this::toCallable).collect(toList()));
  }

  private Callable<Void> toCallable(ConsumableEvent consumableEvent) {
    return () -> {
      eventServiceInvoker.accept(consumableEvent.getEvent());
      consumableEvent.ack();
      return null;
    };
  }
}
