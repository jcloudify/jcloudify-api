package api.jcloudify.app.service;

import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.FileType.BUILT_PACKAGE;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.security.AuthenticatedResourceProvider;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.repository.jpa.EnvironmentBuildRepository;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.EnvironmentBuild;
import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnvironmentBuildService {
  private final ExtendedBucketComponent bucketComponent;
  private final AuthenticatedResourceProvider authenticatedResourceProvider;
  private final EnvironmentService environmentService;
  private final EnvironmentBuildRepository repository;

  public void uploadZippedBuildFile(
      EnvironmentType environmentType, String filename, File zippedBuildFile) {
    Application authenticatedApplication =
        authenticatedResourceProvider.getAuthenticatedApplication();
    String appId = authenticatedApplication.getId();
    String userId = authenticatedApplication.getUserId();
    String environmentId =
        environmentService
            .getUserApplicationEnvironmentByIdAndType(userId, appId, environmentType)
            .getId();
    String bucketKey = getBucketKey(userId, appId, environmentId, BUILT_PACKAGE, filename);
    bucketComponent.upload(zippedBuildFile, bucketKey);
    save(environmentId, bucketKey);
  }

  private void save(String envId, String bucketKey) {
    repository.save(EnvironmentBuild.builder().environmentId(envId).bucketKey(bucketKey).build());
  }
}
