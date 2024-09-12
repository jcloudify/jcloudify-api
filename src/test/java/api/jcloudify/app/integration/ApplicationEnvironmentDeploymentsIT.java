package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PREPROD;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.OTHER_POJA_APPLICATION_ENVIRONMENT_2_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.OTHER_POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.OTHER_POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.getValidPojaConf1;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.ApplicationApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.AppEnvDeployment;
import api.jcloudify.app.endpoint.rest.model.GithubMeta;
import api.jcloudify.app.endpoint.rest.model.GithubUserMeta;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;

@Slf4j
class ApplicationEnvironmentDeploymentsIT extends MockedThirdParties {
  public static final String DEPLOYMENT_1_ID = "deployment_1_id";
  @MockBean ExtendedBucketComponent extendedBucketComponentMock;

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  @BeforeEach
  void setup() throws IOException {
    setUpGithub(githubComponent);
    setUpCloudformationComponent(cloudformationComponent);
    setUpBucketComponent(bucketComponent);
    setUpExtendedBucketComponentMock(extendedBucketComponentMock);
  }

  void setUpExtendedBucketComponentMock(ExtendedBucketComponent extendedBucketComponent)
      throws IOException {
    when(extendedBucketComponent.doesExist(any())).thenReturn(true);
    when(extendedBucketComponent.download(any()))
        .thenReturn(new ClassPathResource("files/poja_1.yml").getFile());
  }

  @Test
  void read_deployments_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new ApplicationApi(apiClient);

    var prodDepls =
        api.getApplicationDeployments(
            JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, PROD, null, null, 1, 10);
    var preprodDepls =
        api.getApplicationDeployments(
            JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, PREPROD, null, null, 1, 10);
    var allDepls =
        api.getApplicationDeployments(
            JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, null, null, null, 1, 10);
    var instantFilteredDepls =
        api.getApplicationDeployments(
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            null,
            Instant.parse("2024-08-01T00:00:00Z"),
            Instant.parse("2024-08-01T23:59:00Z"),
            1,
            10);

    log.info("prod {}", prodDepls);
    log.info("preprod {}", preprodDepls);
    log.info("instant {}", instantFilteredDepls);

    assertTrue(requireNonNull(allDepls.getData()).containsAll(requireNonNull(prodDepls.getData())));
    assertTrue(allDepls.getData().containsAll(requireNonNull(preprodDepls.getData())));
    assertTrue(preprodDepls.getData().contains(preprodDepl()));
    assertFalse(preprodDepls.getData().contains(prodDepl()));
    assertTrue(prodDepls.getData().contains(prodDepl()));
    assertFalse(prodDepls.getData().contains(preprodDepl()));
    assertTrue(requireNonNull(instantFilteredDepls.getData()).contains(prodDepl()));
    assertFalse(instantFilteredDepls.getData().contains(preprodDepl()));
  }

  @Test
  void read_deployment_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new ApplicationApi(apiClient);

    var actual =
        api.getApplicationDeployment(JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, DEPLOYMENT_1_ID);

    assertEquals(prodDepl(), actual);
  }

  @Test
  void read_env_config_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new ApplicationApi(apiClient);
    var expected = new OneOfPojaConf(getValidPojaConf1());

    var actual =
        api.getApplicationDeploymentConfig(JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, DEPLOYMENT_1_ID);
    assertEquals(expected, actual);
  }

  GithubUserMeta johnDoeMetaGhUser() {
    return new GithubUserMeta()
        .email("john.doe@example.com")
        .username("johndoe")
        .githubId("12345678")
        .avatarUrl(URI.create("https://avatars.githubusercontent.com/u/12345678"));
  }

  GithubMeta johnDoeMetaGh() {
    return new GithubMeta()
        .commitBranch("main")
        .commitAuthorName("John Doe")
        .commitMessage("Initial deployment")
        .commitSha("abc123def456")
        .org("poja-org")
        .repoName("repo1")
        .repoOwnerType("organization")
        .repoId("repo1_id")
        .isRepoPrivate(false)
        .repoUrl(URI.create("https://github.com/poja-org/repo1"));
  }

  AppEnvDeployment prodDepl() {
    return new AppEnvDeployment()
        .id(DEPLOYMENT_1_ID)
        .githubMeta(johnDoeMetaGh())
        .creator(johnDoeMetaGhUser())
        .applicationId(OTHER_POJA_APPLICATION_ID)
        .environmentId(OTHER_POJA_APPLICATION_ENVIRONMENT_ID)
        .deployedUrl(URI.create("https://example.com/deploy1"))
        .creationDatetime(Instant.parse("2024-08-01T10:15:00Z"));
  }

  GithubUserMeta janeSmithMetaGhUser() {
    return new GithubUserMeta()
        .email("jane.smith@example.com")
        .username("janesmith")
        .githubId("87654321")
        .avatarUrl(URI.create("https://avatars.githubusercontent.com/u/87654321"));
  }

  GithubMeta janeSmithMetaGh() {
    return new GithubMeta()
        .commitBranch("develop")
        .commitAuthorName("Jane Smith")
        .commitMessage("Bug fixes")
        .commitSha("789ghi012jkl")
        .org("poja-org")
        .repoName("repo2")
        .repoOwnerType("user")
        .repoId("repo2_id")
        .isRepoPrivate(true)
        .repoUrl(URI.create("https://github.com/poja-org/repo2"));
  }

  AppEnvDeployment preprodDepl() {
    return new AppEnvDeployment()
        .id("deployment_2_id")
        .githubMeta(janeSmithMetaGh())
        .creator(janeSmithMetaGhUser())
        .applicationId(OTHER_POJA_APPLICATION_ID)
        .environmentId(OTHER_POJA_APPLICATION_ENVIRONMENT_2_ID)
        .deployedUrl(URI.create("https://example.com/deploy2"))
        .creationDatetime(Instant.parse("2024-08-02T14:30:00Z"));
  }

  GithubUserMeta samBrownMetaGhUser() {
    return new GithubUserMeta()
        .email("sam.brown@example.com")
        .username("sambrown")
        .githubId("98765432")
        .avatarUrl(URI.create("https://avatars.githubusercontent.com/u/98765432"));
  }

  GithubMeta samBrownMetaGh() {
    return new GithubMeta()
        .commitBranch("release")
        .commitAuthorName("Sam Brown")
        .commitMessage("Final deployment")
        .commitSha("mno345pqr678")
        .org("poja-org")
        .repoName("repo3")
        .repoOwnerType("organization")
        .repoId("repo3_id")
        .isRepoPrivate(false)
        .repoUrl(URI.create("https://github.com/poja-org/repo3"));
  }

  AppEnvDeployment archivedDepl() {
    return new AppEnvDeployment()
        .id("deployment_3_id")
        .githubMeta(samBrownMetaGh())
        .creator(samBrownMetaGhUser())
        .applicationId(OTHER_POJA_APPLICATION_ID)
        .environmentId("archived_other_poja_app_env_id")
        .deployedUrl(URI.create("https://example.com/deploy3"))
        .creationDatetime(Instant.parse("2024-08-02T09:00:00Z"));
  }
}
