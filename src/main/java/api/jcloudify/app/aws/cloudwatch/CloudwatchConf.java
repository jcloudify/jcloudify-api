package api.jcloudify.app.aws.cloudwatch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

@Configuration
public class CloudwatchConf {
  private Region region;

  public CloudwatchConf(@Value("${aws.region}") Region region) {
    this.region = region;
  }

  @Bean
  public CloudWatchClient cloudWatchClient() {
    return CloudWatchClient.builder().region(region).build();
  }
}
