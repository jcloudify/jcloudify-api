package api.jcloudify.app.service;

import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.FileType.ZIPPED_PACKAGE;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.security.AuthenticatedResourceProvider;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.repository.model.Application;
import java.io.File;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EnvironmentPackageService {
  private final ExtendedBucketComponent bucketComponent;
  private final AuthenticatedResourceProvider authenticatedResourceProvider;
  private final EnvironmentService environmentService;

  public void uploadZippedPackage(
      EnvironmentType environmentType, String filename, File zippedPackage) {
    Application authenticatedApplication =
        authenticatedResourceProvider.getAuthenticatedApplication();
    String appId = authenticatedApplication.getId();
    String userId = authenticatedApplication.getUserId();
    String environmentId =
        environmentService
            .getUserApplicationEnvironmentByIdAndType(userId, appId, environmentType)
            .getId();
    bucketComponent.upload(
        zippedPackage, getBucketKey(userId, appId, environmentId, ZIPPED_PACKAGE, filename));
  }
}
