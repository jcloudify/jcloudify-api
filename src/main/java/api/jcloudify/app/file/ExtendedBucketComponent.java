package api.jcloudify.app.file;

import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ExtendedBucketComponent {
  private final BucketComponent bucketComponent;

  public final FileHash upload(File file, String key) {
    return bucketComponent.upload(file, key);
  }

  public static String getBucketKey(
      String userId, String appId, String envId, FileType fileType, String filename) {
    return switch (fileType) {
      case POJA_CONF -> String.format(
          "users/%s/apps/%s/envs/%s/poja-files/%s", userId, appId, envId, filename);
      case STACK_EVENT -> String.format(
          "users/%s/apps/%s/envs/%s/stack-events/%s", userId, appId, envId, filename);
    };
  }

  public final File download(String key) {
    return bucketComponent.download(key);
  }
}
