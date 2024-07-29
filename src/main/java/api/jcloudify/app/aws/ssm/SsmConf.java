package api.jcloudify.app.aws.ssm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;

@Configuration
public class SsmConf {
    private final Region region;

    public SsmConf(@Value("${aws.region}") Region region) {
        this.region = region;
    }

    @Bean
    public SsmClient getSsmClient() {
        return SsmClient.builder().region(region).build();
    }
}
