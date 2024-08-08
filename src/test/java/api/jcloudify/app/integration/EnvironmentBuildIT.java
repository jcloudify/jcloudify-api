package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.integration.conf.utils.TestMocks.A_GITHUB_APP_TOKEN;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.BuildHandlingApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.net.URI;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Slf4j
public class EnvironmentBuildIT extends MockedThirdParties {
  private static final String POJA_APPLICATION_REPO_ID = "gh_repository_1_id";

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(A_GITHUB_APP_TOKEN, port);
  }

  @MockBean ExtendedBucketComponent extendedBucketComponent;

  @BeforeEach
  void setup() {
    when(githubComponent.getRepositoryIdByAppToken(A_GITHUB_APP_TOKEN))
        .thenReturn(Optional.of(POJA_APPLICATION_REPO_ID));
    when(extendedBucketComponent.getPresignedPutObjectUri(any()))
        .thenReturn(URI.create("https://localhost:8080"));
  }

  @Test
  void create_upload_uri_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new BuildHandlingApi(apiClient);

    var createdUri = api.createFileUploadUri("mock", "mock", PROD);

    log.info("created uri {}", createdUri);
    assertNotNull(createdUri);
    assertNotNull(createdUri.getUri());
  }
}