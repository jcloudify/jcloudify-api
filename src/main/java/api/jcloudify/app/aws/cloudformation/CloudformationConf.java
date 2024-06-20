package api.jcloudify.app.aws.cloudformation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;

@Configuration
public class CloudformationConf {
  private final Region region;

  public CloudformationConf(@Value("${aws.region}") Region region) {
    this.region = region;
  }

  @Bean
  public CloudFormationClient getCloudformationClient() {
    return CloudFormationClient.builder().region(region).build();
  }
}
