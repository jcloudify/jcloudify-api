package api.jcloudify.app.endpoint.event.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class GetBillingInfoQueryResultRequested extends PojaEvent {
  @JsonCreator
  public GetBillingInfoQueryResultRequested(String queryId) {
    this.queryId = queryId;
  }

  @JsonProperty("query_id")
  private final String queryId;

  @Override
  public Duration maxConsumerDuration() {
    return Duration.ofSeconds(30);
  }

  @Override
  public Duration maxConsumerBackoffBetweenRetries() {
    return Duration.ofSeconds(30);
  }
}
