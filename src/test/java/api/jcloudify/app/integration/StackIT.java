package api.jcloudify.app.integration;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.ApplicationApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.ApplicationBase;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Objects;

import static api.jcloudify.app.endpoint.rest.model.Environment.StateEnum.HEALTHY;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE_PERMISSION;
import static api.jcloudify.app.endpoint.rest.model.StackType.EVENT;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_BUCKET;
import static api.jcloudify.app.integration.conf.utils.TestMocks.BUCKET_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.BUCKET_STACK_NAME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.COMPUTE_PERM_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.COMPUTE_PERM_STACK_NAME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.EVENT_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.EVENT_STACK_NAME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JANE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestUtils.assertThrowsForbiddenException;
import static api.jcloudify.app.integration.conf.utils.TestUtils.ignoreStackIdsAndDatetime;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@AutoConfigureMockMvc
public class StackIT extends FacadeIT {
    @LocalServerPort
    private int port;

    @MockBean
    GithubComponent githubComponent;
    @MockBean
    CloudformationComponent cloudformationComponent;
    @MockBean
    BucketComponent bucketComponent;

    private ApiClient anApiClient(String token) {
        return TestUtils.anApiClient(token, port);
    }

    private static ApplicationBase otherApplication() {
        return new ApplicationBase()
                .id("other_poja_application_id")
                .name("other-poja-test-app")
                .archived(false)
                .githubRepository("https://github.com/joeDoe/other_poja_application")
                .userId(JOE_DOE_ID);
    }

    private static Environment otherEnvironment() {
        return new Environment()
                .id("other_poja_application_environment_id")
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
    @BeforeEach
    void setup() throws MalformedURLException {
        setUpGithub(githubComponent);
        setUpCloudformationComponent(cloudformationComponent);
        setUpBucketComponent(bucketComponent);
    }

    @Test
    void get_stack_list_ok() throws ApiException {
        ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
        ApplicationApi api = new ApplicationApi(joeDoeClient);

        var actual = api.getEnvironmentStacks(POJA_APPLICATION_ID, POJA_APPLICATION_ENVIRONMENT_ID, null, null);
        var actualData = Objects.requireNonNull(actual.getData());

        assertTrue(ignoreStackIdsAndDatetime(actualData).contains(eventStack()));
        assertTrue(
                ignoreStackIdsAndDatetime(actualData).contains(computePermStack()));
        assertTrue(ignoreStackIdsAndDatetime(actualData).contains(bucketStack()));
    }

    @Test
    void get_other_user_stack_list_ko() {
        ApiClient janeDoeClient = anApiClient(JANE_DOE_TOKEN);
        ApplicationApi api = new ApplicationApi(janeDoeClient);

        assertThrowsForbiddenException(
                () -> api.getEnvironmentStacks(POJA_APPLICATION_ID, POJA_APPLICATION_ENVIRONMENT_ID, null, null),
                "Access is denied");
    }

    @Test
    void get_stack_by_id_ok() throws ApiException {
        ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
        ApplicationApi api = new ApplicationApi(joeDoeClient);

        Stack actualEventStack = api.getStackById(POJA_APPLICATION_ID, POJA_APPLICATION_ENVIRONMENT_ID, "event_stack_1_id");
        Stack actualBucketStack = api.getStackById(POJA_APPLICATION_ID, POJA_APPLICATION_ENVIRONMENT_ID, "bucket_stack_1_id");

        assertEquals(actualEventStack, eventStack());
        assertEquals(actualBucketStack, bucketStack());
    }

    @Test
    void get_other_user_stack_by_id_ko() {
        ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
        ApplicationApi api = new ApplicationApi(joeDoeClient);

        assertThrowsForbiddenException(
                () -> api.getStackById(POJA_APPLICATION_ID, POJA_APPLICATION_ENVIRONMENT_ID, "compute_perm_stack_1_id"),
                "Access Denied");
    }
}
