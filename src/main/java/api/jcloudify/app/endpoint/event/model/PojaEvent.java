package api.jcloudify.app.endpoint.event.model;

import api.jcloudify.app.PojaGenerated;
import java.io.Serializable;
import java.time.Duration;

@PojaGenerated
public abstract class PojaEvent implements Serializable {
  public abstract Duration maxConsumerDuration();

  private Duration randomConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(maxConsumerBackoffBetweenRetries().toSeconds());
  }

  public abstract Duration maxConsumerBackoffBetweenRetries();

  public final Duration randomVisibilityTimeout() {
    var eventHandlerInitMaxDuration = Duration.ofSeconds(90); // note(init-visibility)
    return Duration.ofSeconds(
        eventHandlerInitMaxDuration.toSeconds()
            + maxConsumerDuration().toSeconds()
            + randomConsumerBackoffBetweenRetries().toSeconds());
  }
}
