package api.jcloudify.app.endpoint.event.utils;

import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import api.jcloudify.app.endpoint.event.model.ApplicationCrupdated;
import api.jcloudify.app.endpoint.event.model.ComputeStackCrupdateTriggered;
import api.jcloudify.app.repository.model.Stack;
import api.jcloudify.app.service.github.GithubService;
import api.jcloudify.app.service.github.model.CreateRepoResponse;
import api.jcloudify.app.service.github.model.UpdateRepoResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

public class TestMocks {
  public static final String MOCK_POJA_APPLICATION_ID = "mock_poja_application_id";
  public static final String GH_APP_INSTALL_4_ID = "gh_app_install_4_id";
  public static final String MOCK_BUCKET_KEY = "mock/s3/bucket/key";
  public static final Instant MOCK_INSTANT = Instant.parse("2020-01-01T00:00:00Z");

  public static ApplicationCrupdated applicationUpdated() {
    return ApplicationCrupdated.builder()
        .applicationId(MOCK_POJA_APPLICATION_ID)
        .applicationRepoName("mock_poja_application")
        .previousApplicationRepoName(null)
        .description(null)
        .repoPrivate(true)
        .installationId(GH_APP_INSTALL_4_ID)
        .isArchived(false)
        .repoUrl("http://github.com/user/repo")
        .build();
  }

  public static ApplicationCrupdated applicationCreated() {
    return ApplicationCrupdated.builder()
        .applicationId(MOCK_POJA_APPLICATION_ID)
        .applicationRepoName("mock_poja_application")
        .previousApplicationRepoName(null)
        .description(null)
        .repoPrivate(true)
        .installationId(GH_APP_INSTALL_4_ID)
        .isArchived(false)
        .repoUrl(null)
        .build();
  }

  public static UpdateRepoResponse updateRepoResponse() throws URISyntaxException {
    return new UpdateRepoResponse(
        "repo_id", "repo_name", "repo/fullname", null, new URI("https://repo.url"), false);
  }

  public static CreateRepoResponse createRepoResponse() throws URISyntaxException {
    return new CreateRepoResponse(
        "repo_id", "repo_name", "repo/fullname", null, new URI("https://repo.url"), false);
  }

  public static void setUpGithubServiceMock(GithubService mock) throws URISyntaxException {
    when(mock.updateRepoFor(any(), any(), any(), any())).thenReturn(updateRepoResponse());
    when(mock.createRepoFor(any(), any())).thenReturn(createRepoResponse());
  }

  public static ComputeStackCrupdateTriggered computeStackCreated() {
    return ComputeStackCrupdateTriggered.builder()
        .userId(JOE_DOE_ID)
        .appId(POJA_APPLICATION_ID)
        .envId(POJA_APPLICATION_ENVIRONMENT_ID)
        .stackName("poja_app_compute_stack")
        .appEnvDeploymentId("poja_deployment_1_id")
        .build();
  }

  public static Stack newSavedComputeStack() {
    return Stack.builder()
        .name("poja_app_compute_stack")
        .type(COMPUTE)
        .cfStackId("1234")
        .environmentId(POJA_APPLICATION_ENVIRONMENT_ID)
        .applicationId(POJA_APPLICATION_ID)
        .archived(false)
        .build();
  }
}
