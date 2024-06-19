package api.jcloudify.app.aws.cloudformation;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;

@Configuration
@Getter
public class CloudformationConf {
    private final Region region;
    public final String EVENT_STACK_URL;
    public final String COMPUTE_PERMISSION_STACK_URL;
    public final String STORAGE_BUCKET_STACK_URL;
    public final String STORAGE_DATABASE_STACK_URL;

    public CloudformationConf(@Value("${aws.region}") Region region,
                              @Value("${event.stack.url}")String eventStackUrl,
                              @Value("${compute.permission.stack.url}")String computePermissionStackUrl,
                              @Value("${storage.bucket.stack.url}") String storageBucketStackUrl,
                              @Value("${storage.database.stack.url}") String storageDatabaseStackUrl) {
        this.region = region;
        this.EVENT_STACK_URL = eventStackUrl;
        this.COMPUTE_PERMISSION_STACK_URL = computePermissionStackUrl;
        this.STORAGE_BUCKET_STACK_URL = storageBucketStackUrl;
        this.STORAGE_DATABASE_STACK_URL = storageDatabaseStackUrl;
    }

    @Bean
    public CloudFormationClient getCloudformationClient() {
        return CloudFormationClient.builder().region(region).build();
    }
}
