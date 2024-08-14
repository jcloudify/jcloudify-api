package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.file.ExtendedBucketComponent.TEMP_FILES_BUCKET_PREFIX;
import static api.jcloudify.app.file.FileHashAlgorithm.SHA256;
import static api.jcloudify.app.integration.conf.utils.TestMocks.A_GITHUB_APP_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestUtils.assertThrowsBadRequestException;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.rest.api.EnvDeployApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.file.FileHash;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import api.jcloudify.app.repository.jpa.EnvBuildRequestRepository;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@Slf4j
public class EnvironmentBuildIT extends MockedThirdParties {
  private static final String POJA_APPLICATION_REPO_ID = "gh_repository_1_id";
  public static final String MOCK_BUILT_ZIP_PATH = "mock_built_zip.zip";
  public static final String MOCK_BUILT_ZIP_TEST_RESOURCE_PATH = "files/" + MOCK_BUILT_ZIP_PATH;

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(A_GITHUB_APP_TOKEN, port);
  }

  @MockBean ExtendedBucketComponent extendedBucketComponentMock;
  @MockBean EventProducer<?> eventProducerMock;
  @Autowired EnvBuildRequestRepository envBuildRequestRepository;

  @BeforeEach
  void setup() {
    when(githubComponent.getRepositoryIdByAppToken(A_GITHUB_APP_TOKEN))
        .thenReturn(Optional.of(POJA_APPLICATION_REPO_ID));
    when(extendedBucketComponentMock.getPresignedPutObjectUri(any(), any()))
        .thenReturn(URI.create("https://localhost:8080"));
  }

  @Test
  void create_upload_uri_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new EnvDeployApi(apiClient);

    var createdUri = api.createFileUploadUri("mock", "mock", PROD);

    log.info("created uri {}", createdUri);
    assertNotNull(createdUri);
    assertNotNull(createdUri.getUri());
  }

  @Test
  void deploy_env_ko() {
    var apiClient = anApiClient();
    var api = new EnvDeployApi(apiClient);

    assertThrowsBadRequestException(
        () ->
            api.deployEnv(
                "mock",
                "mock",
                PROD,
                new BuiltEnvInfo()
                    .environmentType(PROD)
                    .formattedBucketKey("mock/mock.zip")
                    .id("build_1_id")),
        "EnvBuildRequest has already been sent");
  }

  @Test
  void deploy_env_ok() throws ApiException, IOException {
    when(extendedBucketComponentMock.getFileHash(any()))
        .thenReturn(
            new FileHash(
                SHA256, "cb7a62143f21e9b08cc378371fa34d994943f7f39f6134c03089c0eec50fff16"));
    String bucketKey = TEMP_FILES_BUCKET_PREFIX + MOCK_BUILT_ZIP_PATH;
    when(extendedBucketComponentMock.doesExist(bucketKey)).thenReturn(true);
    when(extendedBucketComponentMock.download(bucketKey))
        .thenReturn(new ClassPathResource(MOCK_BUILT_ZIP_TEST_RESOURCE_PATH).getFile());
    var apiClient = anApiClient();
    var api = new EnvDeployApi(apiClient);

    String id = randomUUID().toString();
    BuiltEnvInfo payload =
        new BuiltEnvInfo().environmentType(PROD).formattedBucketKey(bucketKey).id(id);
    var actual = api.deployEnv("mock", "mock", PROD, payload);
    verify(eventProducerMock, times(1)).accept(anyList());

    assertTrue(envBuildRequestRepository.existsById(requireNonNull(payload.getId())));
    assertEquals(payload, actual);
  }
}
