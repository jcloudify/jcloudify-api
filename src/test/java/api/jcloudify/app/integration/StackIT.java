package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.Environment.StateEnum.HEALTHY;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE_PERMISSION;
import static api.jcloudify.app.endpoint.rest.model.StackType.EVENT;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_BUCKET;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_DATABASE_SQLITE;
import static api.jcloudify.app.integration.conf.utils.TestMocks.BUCKET_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.BUCKET_STACK_NAME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.COMPUTE_PERM_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.COMPUTE_PERM_STACK_NAME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.EVENT_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.EVENT_STACK_NAME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.GH_APP_INSTALL_1_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JANE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JANE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.OTHER_POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.OTHER_POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.eventStackOutputs;
import static api.jcloudify.app.integration.conf.utils.TestMocks.permStackEvents;
import static api.jcloudify.app.integration.conf.utils.TestMocks.stackDeploymentInitiated;
import static api.jcloudify.app.integration.conf.utils.TestUtils.assertThrowsForbiddenException;
import static api.jcloudify.app.integration.conf.utils.TestUtils.ignoreStackIdsAndDatetime;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpExtendedBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.rest.api.StackApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.endpoint.rest.model.GithubRepository;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.InitiateStackDeploymentRequestBody;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
public class StackIT extends MockedThirdParties {
  @MockBean EventProducer eventProducer;
  @MockBean ExtendedBucketComponent extendedBucketComponent;

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, port);
  }

  private static ApplicationBase otherApplication() {
    return new ApplicationBase()
        .id(OTHER_POJA_APPLICATION_ID)
        .name("other-poja-test-app")
        .archived(false)
        .githubRepository(
            new GithubRepository()
                .name("other_poja_application")
                .isPrivate(false)
                .description("a regular poja app")
                .installationId(GH_APP_INSTALL_1_ID))
        .userId(JOE_DOE_ID);
  }

  private static Environment otherEnvironment() {
    return new Environment()
        .id(OTHER_POJA_APPLICATION_ENVIRONMENT_ID)
        .environmentType(PROD)
        .archived(false)
        .state(HEALTHY);
  }

  private Stack bucketStack() {
    return new Stack()
        .id(BUCKET_STACK_ID)
        .name(BUCKET_STACK_NAME)
        .stackType(STORAGE_BUCKET)
        .cfStackId("bucket_stack_aws_id")
        .application(otherApplication())
        .environment(otherEnvironment())
        .creationDatetime(Instant.parse("2023-06-18T10:15:30.00Z"))
        .updateDatetime(Instant.parse("2023-07-18T10:15:30.00Z"));
  }

  private Stack computePermStack() {
    return new Stack()
        .id(COMPUTE_PERM_STACK_ID)
        .name(COMPUTE_PERM_STACK_NAME)
        .stackType(COMPUTE_PERMISSION)
        .cfStackId("compute_perm_stack_aws_id")
        .application(otherApplication())
        .environment(otherEnvironment())
        .creationDatetime(Instant.parse("2023-06-18T10:15:30.00Z"))
        .updateDatetime(Instant.parse("2023-07-18T10:15:30.00Z"));
  }

  private Stack eventStack() {
    return new Stack()
        .id(EVENT_STACK_ID)
        .name(EVENT_STACK_NAME)
        .stackType(EVENT)
        .cfStackId("event_stack_aws_id")
        .application(otherApplication())
        .environment(otherEnvironment())
        .creationDatetime(Instant.parse("2023-06-18T10:15:30.00Z"))
        .updateDatetime(Instant.parse("2023-07-18T10:15:30.00Z"));
  }

  private static InitiateDeployment createStack(String id, StackType stackType) {
    return new InitiateDeployment().stackType(stackType);
  }

  private static InitiateDeployment updateStack(String stackId, StackType stackType) {
    return new InitiateDeployment().stackType(stackType);
  }

  @BeforeEach
  void setup() throws IOException, URISyntaxException {
    setUpGithub(githubComponent);
    setUpCloudformationComponent(cloudformationComponent);
    setUpBucketComponent(bucketComponent);
    setUpExtendedBucketComponent(extendedBucketComponent);
  }

  @Test
  void create_stacks_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    StackApi api = new StackApi(joeDoeClient);

    var actualCreatedStacks =
        api.initiateStackDeployment(
            JOE_DOE_ID,
            POJA_APPLICATION_ID,
            POJA_APPLICATION_ENVIRONMENT_ID,
            new InitiateStackDeploymentRequestBody()
                .data(
                    List.of(
                        createStack("poja_app_event_stack_id", EVENT),
                        createStack("poja_app_perm_stack_id", COMPUTE_PERMISSION),
                        createStack("poja_app_bucket_stack_id", STORAGE_BUCKET),
                        createStack("poja_app_sqlite_stack_id", STORAGE_DATABASE_SQLITE))));
    var actualCreatedStacksData = requireNonNull(actualCreatedStacks.getData());

    assertNotNull(actualCreatedStacksData.getFirst().getCreationDatetime());
    assertTrue(
        ignoreStackIdsAndDatetime(actualCreatedStacksData)
            .contains(stackDeploymentInitiated(EVENT)));
    assertTrue(
        ignoreStackIdsAndDatetime(actualCreatedStacksData)
            .contains(stackDeploymentInitiated(COMPUTE_PERMISSION)));
    assertTrue(
        ignoreStackIdsAndDatetime(actualCreatedStacksData)
            .contains(stackDeploymentInitiated(STORAGE_BUCKET)));
    assertTrue(
        ignoreStackIdsAndDatetime(actualCreatedStacksData)
            .contains(stackDeploymentInitiated(STORAGE_DATABASE_SQLITE)));
  }

  @Test
  void update_stack_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    StackApi api = new StackApi(joeDoeClient);

    var actualCreatedStacks =
        api.initiateStackDeployment(
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
            new InitiateStackDeploymentRequestBody()
                .data(
                    List.of(
                        updateStack(EVENT_STACK_ID, EVENT),
                        updateStack(COMPUTE_PERM_STACK_ID, COMPUTE_PERMISSION),
                        updateStack("bucket_stack_1_id", STORAGE_BUCKET))));
    var actualCreatedStacksData = requireNonNull(actualCreatedStacks.getData());
    var firstData = actualCreatedStacksData.getFirst();
    var allStacks =
        api.getEnvironmentStacks(
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
            null,
            null);

    assertNotNull(firstData.getUpdateDatetime());
    assertNotNull(firstData.getCreationDatetime());
    assertTrue(firstData.getUpdateDatetime().isAfter(firstData.getCreationDatetime()));
    assertEquals(requireNonNull(allStacks.getData()).size(), 3);
  }

  @Test
  void get_stack_list_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    StackApi api = new StackApi(joeDoeClient);

    var actual =
        api.getEnvironmentStacks(
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
            null,
            null);
    var actualData = Objects.requireNonNull(actual.getData());

    assertTrue(actualData.contains(eventStack()));
    assertTrue(actualData.contains(computePermStack()));
    assertTrue(actualData.contains(bucketStack()));
  }

  @Test
  void get_other_user_stack_list_ko() {
    ApiClient janeDoeClient = anApiClient(JANE_DOE_TOKEN);
    StackApi api = new StackApi(janeDoeClient);

    assertThrowsForbiddenException(
        () ->
            api.getEnvironmentStacks(
                JANE_DOE_ID, POJA_APPLICATION_ID, POJA_APPLICATION_ENVIRONMENT_ID, null, null),
        "Bad credentials");
  }

  @Test
  void get_stack_by_id_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    StackApi api = new StackApi(joeDoeClient);

    Stack actualEventStack =
        api.getStackById(
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
            EVENT_STACK_ID);
    Stack actualBucketStack =
        api.getStackById(
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
            "bucket_stack_1_id");

    assertEquals(actualEventStack, eventStack());
    assertEquals(actualBucketStack, bucketStack());
  }

  @Test
  void get_other_user_stack_by_id_ko() {
    ApiClient janeDoeClient = anApiClient(JANE_DOE_TOKEN);
    StackApi api = new StackApi(janeDoeClient);

    assertThrowsForbiddenException(
        () ->
            api.getStackById(
                JANE_DOE_ID,
                POJA_APPLICATION_ID,
                POJA_APPLICATION_ENVIRONMENT_ID,
                COMPUTE_PERM_STACK_ID),
        "Bad credentials");
  }

  @Test
  void get_stack_events_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    StackApi api = new StackApi(joeDoeClient);

    var actual =
        api.getStackEvents(
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
            COMPUTE_PERM_STACK_ID,
            null,
            null);
    var actualData = requireNonNull(actual.getData());

    assertTrue(actualData.containsAll(permStackEvents()));
  }

  @Test
  void get_other_stack_event_ko() {
    ApiClient janeDoeClient = anApiClient(JANE_DOE_TOKEN);
    StackApi api = new StackApi(janeDoeClient);

    assertThrowsForbiddenException(
        () ->
            api.getStackEvents(
                JOE_DOE_ID,
                OTHER_POJA_APPLICATION_ID,
                OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
                COMPUTE_PERM_STACK_ID,
                null,
                null),
        "Bad credentials");
  }

  @Test
  void get_stack_outputs_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    StackApi api = new StackApi(joeDoeClient);

    var actual =
            api.getStackOutputs(
                    JOE_DOE_ID,
                    OTHER_POJA_APPLICATION_ID,
                    OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
                    COMPUTE_PERM_STACK_ID,
                    null,
                    null);
    var actualData = requireNonNull(actual.getData());

    assertTrue(actualData.containsAll(eventStackOutputs()));
  }

  @Test
  void get_other_stack_outputs_ko() {
    ApiClient janeDoeClient = anApiClient(JANE_DOE_TOKEN);
    StackApi api = new StackApi(janeDoeClient);

    assertThrowsForbiddenException(
            () ->
                    api.getStackOutputs(
                            JOE_DOE_ID,
                            OTHER_POJA_APPLICATION_ID,
                            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
                            COMPUTE_PERM_STACK_ID,
                            null,
                            null),
            "Bad credentials");
  }
}
