package api.jcloudify.app.service;

import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.ExtendedBucketComponent.getTempBucketKey;
import static api.jcloudify.app.file.FileType.BUILT_PACKAGE;
import static api.jcloudify.app.file.FileType.DEPLOYMENT_FILE;
import static java.time.Instant.now;
import static java.util.UUID.randomUUID;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.CheckTemplateIntegrityTriggered;
import api.jcloudify.app.endpoint.event.model.PojaEvent;
import api.jcloudify.app.endpoint.rest.model.BuildUploadRequestResponse;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.security.AuthenticatedResourceProvider;
import api.jcloudify.app.endpoint.validator.BuiltEnvInfoValidator;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.EnvBuildRequest;
import api.jcloudify.app.repository.model.Environment;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class EnvironmentBuildService {
  private static final String ZIP_FILE_EXTENSION = ".zip";
  private final ExtendedBucketComponent bucketComponent;
  private final AuthenticatedResourceProvider authenticatedResourceProvider;
  private final EnvironmentService environmentService;
  private final EventProducer<PojaEvent> eventProducer;
  private final EnvBuildRequestService envBuildRequestService;
  private final BuiltEnvInfoValidator builtEnvInfoValidator;

  public BuildUploadRequestResponse getZippedBuildUploadRequestDetails(
      EnvironmentType environmentType) {
    Application authenticatedApplication =
        authenticatedResourceProvider.getAuthenticatedApplication();
    String appId = authenticatedApplication.getId();
    String userId = authenticatedApplication.getUserId();
    Environment env =
        environmentService.getUserApplicationEnvironmentByIdAndType(userId, appId, environmentType);
    String environmentId = env.getId();
    String bucketKey = getTempBucketKey(ZIP_FILE_EXTENSION);
    Duration fifteenMinutes = Duration.ofMinutes(15);
    var uri = bucketComponent.getPresignedPutObjectUri(bucketKey, fifteenMinutes);
    var buildTemplateFilename = env.getLatestDeploymentConf().getBuildTemplateFile();
    var buildTemplateUri =
        bucketComponent.presignGetObject(
            getBucketKey(userId, appId, environmentId, DEPLOYMENT_FILE, buildTemplateFilename),
            fifteenMinutes);
    return new BuildUploadRequestResponse()
        .uri(uri)
        .filename(bucketKey)
        .buildTemplateFileUri(buildTemplateUri);
  }

  @Transactional
  public void initiateDeployment(BuiltEnvInfo builtEnvInfo) {
    if (envBuildRequestService.existsById(builtEnvInfo.getId())) {
      throw new BadRequestException("EnvBuildRequest has already been sent");
    }
    builtEnvInfoValidator.accept(builtEnvInfo);
    Application authenticatedApplication =
        authenticatedResourceProvider.getAuthenticatedApplication();
    String appId = authenticatedApplication.getId();
    String userId = authenticatedApplication.getUserId();
    var environment =
        environmentService.getUserApplicationEnvironmentByIdAndType(
            userId, appId, builtEnvInfo.getEnvironmentType());
    var latestDeploymentConf = environment.getLatestDeploymentConf();
    var formattedOriginalTemplateFilename =
        getBucketKey(
            userId,
            appId,
            environment.getId(),
            DEPLOYMENT_FILE,
            latestDeploymentConf.getBuildTemplateFile());
    String builtPackageBucketKey =
        getBucketKey(
            userId,
            appId,
            environment.getId(),
            BUILT_PACKAGE,
            "build" + randomUUID() + ZIP_FILE_EXTENSION);
    copyFromTempToRealKey(builtEnvInfo.getFormattedBucketKey(), builtPackageBucketKey);
    envBuildRequestService.save(
        EnvBuildRequest.builder()
            .id(builtEnvInfo.getId())
            .appId(appId)
            .userId(userId)
            .builtZipFileKey(builtPackageBucketKey)
            .creationTimestamp(now())
            .build());
    eventProducer.accept(
        List.of(
            CheckTemplateIntegrityTriggered.builder()
                .userId(userId)
                .appId(appId)
                .envId(environment.getId())
                .builtEnvInfo(builtEnvInfo)
                .builtProjectBucketKey(builtPackageBucketKey)
                .templateFileBucketKey(formattedOriginalTemplateFilename)
                .deploymentConfId(latestDeploymentConf.getId())
                .build()));
  }

  private void copyFromTempToRealKey(String tempFilePath, String realFilePath) {
    bucketComponent.moveFile(tempFilePath, realFilePath);
    bucketComponent.deleteFile(tempFilePath);
  }
}
