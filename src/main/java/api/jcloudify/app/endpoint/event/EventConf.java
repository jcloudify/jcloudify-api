package api.jcloudify.app.endpoint.event;

import api.jcloudify.app.PojaGenerated;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@PojaGenerated
@Configuration
public class EventConf {
  private final Region region;
  @Getter private final String sqsQueue;

  public EventConf(
      @Value("${aws.region}") Region region, @Value("${aws.sqs.queue.url}") String sqsQueue) {
    this.region = region;
    this.sqsQueue = sqsQueue;
  }

  @Bean
  public SqsClient getSqsClient() {
    return SqsClient.builder().region(region).build();
  }
}
