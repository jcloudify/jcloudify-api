package api.jcloudify.app.endpoint.event.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Duration;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Builder
public class CrupdateLogStreamTriggered extends PojaEvent{
    private String logGroupName;
    private String bucketKey;
    @Override
    public Duration maxConsumerDuration() {
        return Duration.ofMinutes(2);
    }

    @Override
    public Duration maxConsumerBackoffBetweenRetries() {
        return Duration.ofSeconds(30);
    }
}
