package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.INDEPENDENT_STACKS_DEPLOYED;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.INDEPENDENT_STACKS_DEPLOYMENT_INITIATED;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.INDEPENDENT_STACKS_DEPLOYMENT_IN_PROGRESS;
import static api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum.TEMPLATE_FILE_CHECK_IN_PROGRESS;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PREPROD;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.endpoint.rest.model.ExecutionType.ASYNCHRONOUS;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
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
import api.jcloudify.app.endpoint.rest.model.DeploymentState;
import api.jcloudify.app.endpoint.rest.model.GithubMeta;
import api.jcloudify.app.endpoint.rest.model.GithubMetaCommit;
import api.jcloudify.app.endpoint.rest.model.GithubMetaRepo;
import api.jcloudify.app.endpoint.rest.model.GithubUserMeta;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
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
                JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, PROD, null, null, 1, 10)
            .getData();
    var preprodDepls =
        api.getApplicationDeployments(
                JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, PREPROD, null, null, 1, 10)
            .getData();
    // allDepls pageSize is set to max pageSize=500 in order to get all prod and preprod depls
    // although testData should not reach 500
    var allDepls =
        api.getApplicationDeployments(
                JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, null, null, null, 1, 500)
            .getData();
    var instantFilteredDepls =
        api.getApplicationDeployments(
                JOE_DOE_ID,
                OTHER_POJA_APPLICATION_ID,
                null,
                Instant.parse("2024-08-01T00:00:00Z"),
                Instant.parse("2024-08-01T23:59:00Z"),
                1,
                10)
            .getData();

    assertTrue(requireNonNull(allDepls).containsAll(requireNonNull(prodDepls)));
    assertTrue(allDepls.containsAll(requireNonNull(preprodDepls)));
    assertTrue(preprodDepls.contains(preprodDepl()));
    assertFalse(preprodDepls.contains(prodDepl()));
    assertTrue(prodDepls.contains(prodDepl()));
    assertFalse(prodDepls.contains(preprodDepl()));
    assertTrue(requireNonNull(instantFilteredDepls).contains(prodDepl()));
    assertFalse(instantFilteredDepls.contains(preprodDepl()));
  }

  @Test
  void read_deployments_is_well_ordered_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new ApplicationApi(apiClient);

    var allDepls =
        api.getApplicationDeployments(
            JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, null, null, null, 1, 10);
    List<AppEnvDeployment> data = allDepls.getData();
    var sorted =
        data.stream().sorted(Comparator.comparing(AppEnvDeployment::getCreationDatetime)).toList();

    assertEquals(sorted, data);
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

  @Test
  void read_deployment_states_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new ApplicationApi(apiClient);

    var actual =
        api.getApplicationDeploymentStates(JOE_DOE_ID, OTHER_POJA_APPLICATION_ID, DEPLOYMENT_1_ID);
    assertEquals(deploymentState(), actual);
  }

  DeploymentState deploymentState() {
    return new DeploymentState()
        .id("other_poja_application_deployment_state_1_id")
        .timestamp(Instant.parse("2024-09-01T08:50:00Z"))
        .progressionStatus(TEMPLATE_FILE_CHECK_IN_PROGRESS)
        .executionType(ASYNCHRONOUS)
        .nextState(
            new DeploymentState()
                .id("other_poja_application_deployment_state_2_id")
                .timestamp(Instant.parse("2024-09-01T08:51:00Z"))
                .progressionStatus(INDEPENDENT_STACKS_DEPLOYMENT_INITIATED)
                .executionType(ASYNCHRONOUS)
                .nextState(
                    new DeploymentState()
                        .id("other_poja_application_deployment_state_3_id")
                        .timestamp(Instant.parse("2024-09-01T08:51:15Z"))
                        .progressionStatus(INDEPENDENT_STACKS_DEPLOYMENT_IN_PROGRESS)
                        .executionType(ASYNCHRONOUS)
                        .nextState(
                            new DeploymentState()
                                .id("other_poja_application_deployment_state_4_id")
                                .timestamp(Instant.parse("2024-09-01T08:51:35Z"))
                                .progressionStatus(INDEPENDENT_STACKS_DEPLOYED)
                                .executionType(ASYNCHRONOUS)
                                .nextState(
                                    new DeploymentState()
                                        .id("other_poja_application_deployment_state_5_id")
                                        .timestamp(Instant.parse("2024-09-01T08:52:00Z"))
                                        .progressionStatus(COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS)
                                        .executionType(ASYNCHRONOUS)
                                        .nextState(null)))));
  }

  GithubUserMeta johnDoeMetaGhUser() {
    return new GithubUserMeta()
        .login("johndoe")
        .email("john.doe@example.com")
        .name("John Doe")
        .githubId("12345678")
        .avatarUrl(URI.create("https://avatars.githubusercontent.com/u/12345678"))
        .isJcBot(false);
  }

  GithubMeta johnDoeMetaGh() {
    GithubMetaRepo repo = new GithubMetaRepo().ownerName("poja-org").name("repo1");

    GithubMetaCommit commit =
        new GithubMetaCommit()
            .branch("prod")
            .committer(johnDoeMetaGhUser())
            .message("Initial deployment")
            .sha("abc123def456")
            .url(URI.create("https://github.com/poja-org/repo1/commit/abc123def456"));

    return new GithubMeta().commit(commit).repo(repo);
  }

  AppEnvDeployment prodDepl() {
    return new AppEnvDeployment()
        .id("deployment_1_id")
        .githubMeta(johnDoeMetaGh())
        .applicationId("other_poja_application_id")
        .environmentId("other_poja_application_environment_id")
        .deployedUrl(URI.create("https://example.com/deploy1"))
        .creationDatetime(Instant.parse("2024-08-01T10:15:00Z"));
  }

  GithubUserMeta janeSmithMetaGhUser() {
    return new GithubUserMeta()
        .login("janesmith")
        .email("jane.smith@example.com")
        .name("Jane Smith")
        .githubId("87654321")
        .avatarUrl(URI.create("https://avatars.githubusercontent.com/u/87654321"))
        .isJcBot(false);
  }

  GithubMeta janeSmithMetaGh() {
    GithubMetaRepo repo = new GithubMetaRepo().ownerName("poja-org").name("repo2");

    GithubMetaCommit commit =
        new GithubMetaCommit()
            .branch("preprod")
            .committer(janeSmithMetaGhUser())
            .message("Bug fixes")
            .sha("789ghi012jkl")
            .url(URI.create("https://github.com/poja-org/repo2/commit/789ghi012jkl"));

    return new GithubMeta().commit(commit).repo(repo);
  }

  AppEnvDeployment preprodDepl() {
    return new AppEnvDeployment()
        .id("deployment_2_id")
        .githubMeta(janeSmithMetaGh())
        .applicationId("other_poja_application_id")
        .environmentId("other_poja_application_environment_2_id")
        .deployedUrl(URI.create("https://example.com/deploy2"))
        .creationDatetime(Instant.parse("2024-08-02T14:30:00Z"));
  }

  GithubUserMeta samBrownMetaGhUser() {
    return new GithubUserMeta()
        .login("sambrown")
        .email("sam.brown@example.com")
        .name("Sam Brown")
        .githubId("98765432")
        .avatarUrl(URI.create("https://avatars.githubusercontent.com/u/98765432"))
        .isJcBot(false);
  }

  GithubMeta samBrownMetaGh() {
    GithubMetaRepo repo = new GithubMetaRepo().ownerName("poja-org").name("repo3");

    GithubMetaCommit commit =
        new GithubMetaCommit()
            .branch("prod")
            .committer(samBrownMetaGhUser())
            .message("Final deployment")
            .sha("mno345pqr678")
            .url(URI.create("https://github.com/poja-org/repo3/commit/mno345pqr678"));

    return new GithubMeta().commit(commit).repo(repo);
  }

  AppEnvDeployment archivedDepl() {
    return new AppEnvDeployment()
        .id("deployment_3_id")
        .githubMeta(samBrownMetaGh())
        .applicationId("other_poja_application_id")
        .environmentId("archived_other_poja_app_env_id")
        .deployedUrl(URI.create("https://example.com/deploy3"))
        .creationDatetime(Instant.parse("2024-08-02T09:00:00Z"));
  }
}
