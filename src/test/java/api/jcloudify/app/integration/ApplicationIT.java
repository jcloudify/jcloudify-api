package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE_PERMISSION;
import static api.jcloudify.app.endpoint.rest.model.StackType.EVENT;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_BUCKET;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_DATABASE_POSTGRES;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_DATABASE_SQLITE;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_CREATION_DATETIME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_CF_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_CREATED_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.applicationToCreate;
import static api.jcloudify.app.integration.conf.utils.TestMocks.applicationToUpdate;
import static api.jcloudify.app.integration.conf.utils.TestMocks.janePojaApplication;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joePojaApplication1;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joePojaApplication2;
import static api.jcloudify.app.integration.conf.utils.TestMocks.pojaAppProdEnvironment;
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
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentRequestBody;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.PageFromOne;
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

  private static Stack stackDeploymentInitiated(StackType stackType) {
    return new Stack()
        .id(POJA_CREATED_STACK_ID)
        .name("prod-" + stackType.getValue().toLowerCase().replace("_", "-") + "-poja-test-app")
        .cfStackId(POJA_CF_STACK_ID)
        .stackType(stackType)
        .application(applicationToUpdate())
        .environment(pojaAppProdEnvironment());
  }

  private static InitiateDeployment initiateStackDeployment(StackType stackType) {
    return new InitiateDeployment().stackType(stackType);
  }

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  @BeforeEach
  void setup() throws MalformedURLException {
    setUpGithub(githubComponent);
    setUpCloudformationComponent(cloudformationComponent);
    setUpBucketComponent(bucketComponent);
  }

  @Test
  void initiate_event_stack_deployment_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    ApplicationApi api = new ApplicationApi(joeDoeClient);

    var actual =
        api.initiateStackDeployment(
            POJA_APPLICATION_ID,
            POJA_APPLICATION_ENVIRONMENT_ID,
            new InitiateStackDeploymentRequestBody()
                .data(
                    List.of(
                        initiateStackDeployment(EVENT),
                        initiateStackDeployment(COMPUTE_PERMISSION),
                        initiateStackDeployment(STORAGE_BUCKET),
                        initiateStackDeployment(STORAGE_DATABASE_POSTGRES),
                        initiateStackDeployment(STORAGE_DATABASE_SQLITE))));
    var actualData = requireNonNull(actual.getData());

    assertTrue(ignoreIds(actualData).contains(stackDeploymentInitiated(EVENT)));
    assertTrue(ignoreIds(actualData).contains(stackDeploymentInitiated(COMPUTE_PERMISSION)));
    assertTrue(ignoreIds(actualData).contains(stackDeploymentInitiated(STORAGE_BUCKET)));
    assertTrue(ignoreIds(actualData).contains(stackDeploymentInitiated(STORAGE_DATABASE_POSTGRES)));
    assertTrue(ignoreIds(actualData).contains(stackDeploymentInitiated(STORAGE_DATABASE_SQLITE)));
  }

  @Test
  void crupdate_applications_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    ApplicationApi api = new ApplicationApi(joeDoeClient);
    ApplicationBase toCreate = applicationToCreate();

    var createApplicationResponse =
        api.crupdateApplications(new CrupdateApplicationsRequestBody().data(List.of(toCreate)));
    List<ApplicationBase> updatedPayload =
        List.of(
            toApplicationBase(
                requireNonNull(createApplicationResponse.getData())
                    .getFirst()
                    .name(randomUUID().toString())));

    var updateApplicationResponse =
        api.crupdateApplications(new CrupdateApplicationsRequestBody().data(updatedPayload));
    var updateApplicationResponseData =
        requireNonNull(updateApplicationResponse.getData()).stream()
            .map(ApplicationIT::ignoreIds)
            .toList();

    List<Application> expectedResponseData =
        updatedPayload.stream()
            .map(ApplicationIT::toApplicationWithIgnoredEnvironments)
            .map(ApplicationIT::ignoreIds)
            .toList();
    assertTrue(updateApplicationResponseData.containsAll(expectedResponseData));
  }

  @Test
  void get_all_applications_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    ApplicationApi api = new ApplicationApi(joeDoeClient);

    var userIdFilteredPagedResponse =
        api.getApplications(
            null, JOE_DOE_ID, new PageFromOne(1).getValue(), new BoundedPageSize(10).getValue());
    List<Application> userIdFilteredPagedResponseData =
        requireNonNull(userIdFilteredPagedResponse.getData());
    var nameFilteredPagedResponse =
        api.getApplications(
            "2", JOE_DOE_ID, new PageFromOne(1).getValue(), new BoundedPageSize(10).getValue());
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

  private static List<Stack> ignoreIds(List<Stack> stacks) {
    return stacks.stream().map(stack -> stack.id(POJA_CREATED_STACK_ID)).toList();
  }

  private static Application ignoreIds(Application application) {
    return application.id(POJA_APPLICATION_ID).creationDatetime(POJA_APPLICATION_CREATION_DATETIME);
  }
}
