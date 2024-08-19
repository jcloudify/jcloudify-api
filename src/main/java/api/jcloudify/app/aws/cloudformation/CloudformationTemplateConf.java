package api.jcloudify.app.aws.cloudformation;

import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.FileType.DEPLOYMENT_FILE;

import api.jcloudify.app.file.ExtendedBucketComponent;
import java.net.URI;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CloudformationTemplateConf {
  private final ExtendedBucketComponent extendedBucketComponent;
  private final Duration TEMPLATE_PRESIGNED_URL_DURATION = Duration.ofMinutes(10);

  public URI getCloudformationTemplateUrl(
      String userId, String appId, String envId, String filename) {
    String formattedBucketKey = getBucketKey(userId, appId, envId, DEPLOYMENT_FILE) + filename;
    return extendedBucketComponent.presignGetObject(
        formattedBucketKey, TEMPLATE_PRESIGNED_URL_DURATION);
  }
}
