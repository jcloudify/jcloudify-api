package api.jcloudify.app.aws.cloudwatch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

@Configuration
public class CloudwatchConf {
  private final Region region;

  public CloudwatchConf(@Value("${aws.region}") Region region) {
    this.region = region;
  }

  @Bean
  public CloudWatchLogsClient getCloudwatchLogsClient() {
    return CloudWatchLogsClient.builder().region(region).build();
  }
}
