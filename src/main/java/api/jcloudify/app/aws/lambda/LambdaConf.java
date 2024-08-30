package api.jcloudify.app.aws.lambda;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class LambdaConf {
  private final Region region;

  public LambdaConf(@Value("${aws.region}") Region region) {
    this.region = region;
  }

  @Bean
  public LambdaClient lambdaClient() {
    return LambdaClient.builder().region(region).build();
  }
}
