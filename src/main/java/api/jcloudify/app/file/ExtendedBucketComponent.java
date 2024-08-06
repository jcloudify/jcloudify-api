package api.jcloudify.app.file;

import static api.jcloudify.app.file.FileType.DEPLOYMENT_FOLDER;

import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@AllArgsConstructor
@Component
public class ExtendedBucketComponent {
  private final BucketComponent bucketComponent;
  private final BucketConf bucketConf;

  public final FileHash upload(File file, String key) {
    return bucketComponent.upload(file, key);
  }

  public boolean doesExist(String bucketKey) {
    try {
      HeadObjectRequest headObjectRequest =
          HeadObjectRequest.builder().bucket(bucketConf.getBucketName()).key(bucketKey).build();

      HeadObjectResponse headObjectResponse =
          bucketConf.getS3Client().headObject(headObjectRequest);
      return headObjectResponse != null;
    } catch (NoSuchKeyException e) {
      return false;
    }
  }

  public static String getBucketKey(
      String userId, String appId, String envId, FileType fileType, String filename) {
    return switch (fileType) {
      case POJA_CONF -> String.format(
          "users/%s/apps/%s/envs/%s/poja-files/%s", userId, appId, envId, filename);
      case BUILT_PACKAGE -> String.format(
          "users/%s/apps/%s/envs/%s/built-packages/%s", userId, appId, envId, filename);
      case DEPLOYMENT_FOLDER -> throw new UnsupportedOperationException();
    };
  }

  public static String getBucketKey(String userId, String appId, String envId, FileType fileType) {
    if (DEPLOYMENT_FOLDER == fileType) {
      return String.format("users/%s/apps/%s/envs/%s/deployment-files/", userId, appId, envId);
    }
    throw new UnsupportedOperationException();
  }

  public final File download(String key) {
    return bucketComponent.download(key);
  }
}
