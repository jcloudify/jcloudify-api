package api.jcloudify.app.file;

import static api.jcloudify.app.file.hash.FileHashAlgorithm.SHA256;
import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static java.util.UUID.randomUUID;

import api.jcloudify.app.file.bucket.BucketComponent;
import api.jcloudify.app.file.bucket.BucketConf;
import api.jcloudify.app.file.hash.FileHash;
import api.jcloudify.app.model.exception.ApiException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

@AllArgsConstructor
@Component
@Slf4j
public class ExtendedBucketComponent {
  private static final String APPLICATION_ZIP_CONTENT_TYPE = "application/zip";
  public static final String TEMP_FILES_BUCKET_PREFIX = "tmp-";
  private final BucketComponent bucketComponent;
  private final BucketConf bucketConf;

  public final FileHash upload(File file, String key) {
    return bucketComponent.upload(file, key);
  }

  public final URI getPresignedPutObjectUri(String key, Duration expiration) {
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
                  .signatureDuration(expiration)
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

  public static String getTempBucketKey(String fileExtensionWithDot) {
    return String.format(TEMP_FILES_BUCKET_PREFIX + "%s", randomUUID() + fileExtensionWithDot);
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

  public URI presignGetObject(String key, Duration expiration) {
    try {
      return bucketComponent.presign(key, expiration).toURI();
    } catch (URISyntaxException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  public FileHash moveFile(String from, String to) {
    var copy =
        bucketConf
            .getS3TransferManager()
            .copy(
                req ->
                    req.copyObjectRequest(
                            copyReq ->
                                copyReq
                                    .sourceKey(from)
                                    .sourceBucket(bucketConf.getBucketName())
                                    .destinationBucket(bucketConf.getBucketName())
                                    .destinationKey(to))
                        .addTransferListener(LoggingTransferListener.create()));
    var copied = copy.completionFuture().join();
    return new FileHash(SHA256, copied.response().copyObjectResult().checksumSHA256());
  }

  public String deleteFile(String key) {
    bucketConf
        .getS3Client()
        .deleteObject(delReq -> delReq.bucket(bucketConf.getBucketName()).key(key));
    log.info("deleted {}", key);
    return key;
  }
}
