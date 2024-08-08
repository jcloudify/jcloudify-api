package api.jcloudify.app.service;

import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.FileType.BUILT_PACKAGE;
import static java.util.UUID.randomUUID;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.model.FileUploadRequestResponse;
import api.jcloudify.app.endpoint.rest.security.AuthenticatedResourceProvider;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.repository.model.Application;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnvironmentBuildService {
  private final ExtendedBucketComponent bucketComponent;
  private final AuthenticatedResourceProvider authenticatedResourceProvider;
  private final EnvironmentService environmentService;

  public FileUploadRequestResponse getZippedBuildUploadRequestDetails(
      EnvironmentType environmentType) {
    Application authenticatedApplication =
        authenticatedResourceProvider.getAuthenticatedApplication();
    String appId = authenticatedApplication.getId();
    String userId = authenticatedApplication.getUserId();
    String environmentId =
        environmentService
            .getUserApplicationEnvironmentByIdAndType(userId, appId, environmentType)
            .getId();
    String bucketKey =
        getBucketKey(userId, appId, environmentId, BUILT_PACKAGE, "build" + randomUUID() + ".zip");
    var uri = bucketComponent.getPresignedPutObjectUri(bucketKey);
    return new FileUploadRequestResponse().uri(uri).filename(bucketKey);
  }
}
