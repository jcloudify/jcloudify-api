package api.jcloudify.app.file;

import static api.jcloudify.app.file.FileHashAlgorithm.SHA256;
import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.model.exception.ApiException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@AllArgsConstructor
@Component
public class ExtendedBucketComponent {
  private static final String APPLICATION_ZIP_CONTENT_TYPE = "application/zip";
  private final BucketComponent bucketComponent;
  private final BucketConf bucketConf;

  public final FileHash upload(File file, String key) {
    return bucketComponent.upload(file, key);
  }

  public final URI getPresignedPutObjectUri(String key) {
    try {
      return bucketConf
          .getS3Presigner()
          .presignPutObject(
              PutObjectPresignRequest.builder()
                  .putObjectRequest(
                      req ->
                          req.bucket(bucketConf.getBucketName())
                              .key(key)
                              .contentType(APPLICATION_ZIP_CONTENT_TYPE))
                  .signatureDuration(Duration.ofMinutes(2))
                  .build())
          .url()
          .toURI();
    } catch (URISyntaxException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
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
      case POJA_CONF ->
          String.format("users/%s/apps/%s/envs/%s/poja-files/%s", userId, appId, envId, filename);
      case BUILT_PACKAGE ->
          String.format(
              "users/%s/apps/%s/envs/%s/built-packages/%s", userId, appId, envId, filename);
      case DEPLOYMENT_FILE ->
          String.format(
              "users/%s/apps/%s/envs/%s/deployment-files/%s", userId, appId, envId, filename);
    };
  }

  public static String getBucketKey(String userId, String appId, String envId, FileType fileType) {
    return switch (fileType) {
      case POJA_CONF -> String.format("users/%s/apps/%s/envs/%s/poja-files/", userId, appId, envId);
      case BUILT_PACKAGE ->
          String.format("users/%s/apps/%s/envs/%s/built-packages/", userId, appId, envId);
      case DEPLOYMENT_FILE ->
          String.format("users/%s/apps/%s/envs/%s/deployment-files/", userId, appId, envId);
    };
  }

  public final File download(String key) {
    return bucketComponent.download(key);
  }

  public final FileHash getFileHash(String bucketKey) {
    HeadObjectRequest headObjectRequest =
        HeadObjectRequest.builder().bucket(bucketConf.getBucketName()).key(bucketKey).build();

    String sha256 = bucketConf.getS3Client().headObject(headObjectRequest).checksumSHA256();
    return new FileHash(SHA256, sha256);
  }
}
