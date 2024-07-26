package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_CREATION_DATETIME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.applicationToCreate;
import static api.jcloudify.app.integration.conf.utils.TestMocks.janePojaApplication;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joePojaApplication1;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joePojaApplication2;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.ApplicationApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.Application;
import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplicationsRequestBody;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
class ApplicationIT extends FacadeIT {
  @LocalServerPort private int port;

  @MockBean GithubComponent githubComponent;
  @MockBean CloudformationComponent cloudformationComponent;
  @MockBean BucketComponent bucketComponent;

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  @BeforeEach
  void setup() throws IOException {
    setUpGithub(githubComponent);
    setUpCloudformationComponent(cloudformationComponent);
    setUpBucketComponent(bucketComponent);
  }

  @Test
  void crupdate_applications_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    ApplicationApi api = new ApplicationApi(joeDoeClient);
    ApplicationBase toCreate = applicationToCreate();

    var createApplicationResponse =
        api.crupdateApplications(
            JOE_DOE_ID, new CrupdateApplicationsRequestBody().data(List.of(toCreate)));
    List<ApplicationBase> updatedPayload =
        List.of(
            toApplicationBase(
                requireNonNull(createApplicationResponse.getData())
                    .getFirst()
                    .name(randomUUID().toString())));

    var updateApplicationResponse =
        api.crupdateApplications(
            JOE_DOE_ID, new CrupdateApplicationsRequestBody().data(updatedPayload));
    var updateApplicationResponseData =
        requireNonNull(updateApplicationResponse.getData()).stream()
            .map(ApplicationIT::ignoreIds)
            .map(ApplicationIT::ignoreRepositoryUrls)
            .toList();

    List<Application> expectedResponseData =
        updatedPayload.stream()
            .map(ApplicationIT::toApplicationWithIgnoredEnvironments)
            .map(ApplicationIT::ignoreIds)
            .map(ApplicationIT::ignoreRepositoryUrls)
            .toList();
    assertTrue(updateApplicationResponseData.containsAll(expectedResponseData));
  }

  @Test
  void get_all_applications_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    ApplicationApi api = new ApplicationApi(joeDoeClient);

    var userIdFilteredPagedResponse =
        api.getApplications(
            JOE_DOE_ID, null, new PageFromOne(1).getValue(), new BoundedPageSize(10).getValue());
    List<Application> userIdFilteredPagedResponseData =
        requireNonNull(userIdFilteredPagedResponse.getData());
    var nameFilteredPagedResponse =
        api.getApplications(
            JOE_DOE_ID, "2", new PageFromOne(1).getValue(), new BoundedPageSize(10).getValue());
    List<Application> nameFilteredPagedResponseData =
        requireNonNull(nameFilteredPagedResponse.getData());

    assertTrue(userIdFilteredPagedResponseData.contains(joePojaApplication1()));
    assertFalse(userIdFilteredPagedResponseData.contains(janePojaApplication()));
    assertTrue(nameFilteredPagedResponseData.contains(joePojaApplication2()));
    assertFalse(nameFilteredPagedResponseData.contains(joePojaApplication1()));
    assertEquals(1, nameFilteredPagedResponse.getCount());
  }

  private static ApplicationBase toApplicationBase(Application application) {
    return new ApplicationBase()
        .id(application.getId())
        .name(application.getName())
        .archived(application.getArchived())
        .githubRepository(application.getGithubRepository())
        .userId(application.getUserId());
  }

  private static Application toApplicationWithIgnoredEnvironments(ApplicationBase base) {
    return new Application()
        .id(base.getId())
        .name(base.getName())
        .archived(base.getArchived())
        .githubRepository(base.getGithubRepository())
        .userId(base.getUserId())
        .environments(List.of());
  }

  private static Application ignoreIds(Application application) {
    return application.id(POJA_APPLICATION_ID).creationDatetime(POJA_APPLICATION_CREATION_DATETIME);
  }

  private static Application ignoreRepositoryUrls(Application application) {
    return application.repositoryUrl(null);
  }
}
