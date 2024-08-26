package api.jcloudify.app.service;

import static api.jcloudify.app.endpoint.event.model.enums.IndependentStacksStateEnum.NOT_READY;
import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.ExtendedBucketComponent.getTempBucketKey;
import static api.jcloudify.app.file.FileHashAlgorithm.SHA256;
import static api.jcloudify.app.file.FileType.BUILT_PACKAGE;
import static api.jcloudify.app.file.FileType.DEPLOYMENT_FILE;
import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static java.nio.file.Files.createTempDirectory;
import static java.time.Instant.now;
import static java.util.UUID.randomUUID;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.AppEnvDeployRequested;
import api.jcloudify.app.endpoint.event.model.PojaEvent;
import api.jcloudify.app.endpoint.rest.model.BuildUploadRequestResponse;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.security.AuthenticatedResourceProvider;
import api.jcloudify.app.endpoint.validator.BuiltEnvInfoValidator;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.file.FileHash;
import api.jcloudify.app.file.FileHasher;
import api.jcloudify.app.file.FileWriter;
import api.jcloudify.app.model.exception.ApiException;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.EnvBuildRequest;
import api.jcloudify.app.repository.model.Environment;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class EnvironmentBuildService {
  public static final String TEMPLATE_YML_PATH_FROM_BUILD_FOLDER_ROOT = ".aws-build/template.yml";
  private static final String ZIP_FILE_EXTENSION = ".zip";
  private final ExtendedBucketComponent bucketComponent;
  private final AuthenticatedResourceProvider authenticatedResourceProvider;
  private final EnvironmentService environmentService;
  private final FileWriter fileWriter;
  private final FileHasher fileHasher;
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
    var originalTemplateFileHash = bucketComponent.getFileHash(formattedOriginalTemplateFilename);
    // compareWithOriginalTemplate(builtEnvInfo, originalTemplateFileHash);
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
            AppEnvDeployRequested.builder()
                .userId(userId)
                .builtEnvInfo(builtEnvInfo)
                .deploymentConfId(latestDeploymentConf.getId())
                .requestInstant(now())
                .builtZipFormattedFilekey(builtPackageBucketKey)
                .envId(environment.getId())
                .appId(appId)
                .independentStacksStates(NOT_READY)
                .build()));
  }

  private void copyFromTempToRealKey(String tempFilePath, String realFilePath) {
    bucketComponent.moveFile(tempFilePath, realFilePath);
    bucketComponent.deleteFile(tempFilePath);
  }

  private void compareWithOriginalTemplate(
      BuiltEnvInfo builtEnvInfo, FileHash originalTemplateFileHash) {
    String zippedBuildBucketKey = builtEnvInfo.getFormattedBucketKey();
    var exists = bucketComponent.doesExist(zippedBuildBucketKey);
    if (!exists) {
      throw new NotFoundException("file " + zippedBuildBucketKey + " does not exist");
    }
    var extractedTemplateFile = extractTemplateYmlFileFromZipBuild(zippedBuildBucketKey);
    var newFileSha256 = fileHasher.apply(extractedTemplateFile, SHA256);
    if (!originalTemplateFileHash.equals(newFileSha256)) {
      throw new BadRequestException("file " + extractedTemplateFile + " does not match file hash");
    }
  }

  private File extractTemplateYmlFileFromZipBuild(String zippedBuildBucketKey) {
    try (var zip = new ZipFile(bucketComponent.download(zippedBuildBucketKey)); ) {
      var templateFileEntry = zip.getEntry(TEMPLATE_YML_PATH_FROM_BUILD_FOLDER_ROOT);
      if (templateFileEntry == null) {
        throw new BadRequestException(
            "uploaded file has no " + TEMPLATE_YML_PATH_FROM_BUILD_FOLDER_ROOT);
      }
      return extractTemplateFile(zip, templateFileEntry);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private File extractTemplateFile(ZipFile zip, ZipEntry entry) {
    try (var io = zip.getInputStream(entry); ) {
      return fileWriter.write(
          io, createTempDirectory("template_file").toFile(), "original_template.yml");
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
