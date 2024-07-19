package api.jcloudify.app.file;

import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ExtendedBucketComponent {
  private final BucketComponent bucketComponent;

  public final FileHash upload(
      String userId, String appId, String envId, FileType fileType, String key, File file) {
    return bucketComponent.upload(file, getBucketKey(userId, appId, envId, fileType, key));
  }

  private static String getBucketKey(
      String userId, String appId, String envId, FileType fileType, String filename) {
    return switch (fileType) {
      case POJA_CONF ->
          String.format("/users/%s/apps/%s/envs/%s/poja-files/%s", userId, appId, envId, filename);
    };
  }
}
