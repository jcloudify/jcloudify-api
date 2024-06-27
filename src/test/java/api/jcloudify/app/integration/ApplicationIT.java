package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE_PERMISSION;
import static api.jcloudify.app.endpoint.rest.model.StackType.EVENT;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_BUCKET;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_DATABASE_POSTGRES;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_DATABASE_SQLITE;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_CF_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_CREATED_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.applicationToCreate;
import static api.jcloudify.app.integration.conf.utils.TestMocks.prodEnvironment;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.ApplicationApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.Application;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.model.StackType;
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
import java.util.List;

import static api.jcloudify.app.endpoint.rest.model.StackType.*;
import static api.jcloudify.app.integration.conf.utils.TestMocks.*;
import static api.jcloudify.app.integration.conf.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@AutoConfigureMockMvc
public class ApplicationIT extends FacadeIT {
    @LocalServerPort
    private int port;

    @MockBean
    GithubComponent githubComponent;
    @MockBean
    CloudformationComponent cloudformationComponent;
    @MockBean
    BucketComponent bucketComponent;

    private static Stack stackDeploymentInitiated(StackType stackType) {
        return new Stack()
                .id(POJA_CREATED_STACK_ID)
                .name("prod-" + stackType.getValue().toLowerCase().replace("_", "-") + "-poja-test-app")
                .cfStackId(POJA_CF_STACK_ID)
                .stackType(stackType)
                .application(applicationToUpdate())
                .environment(prodEnvironment());
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

    List<Stack> actual =
        api.initiateStackDeployment(
            POJA_APPLICATION_ID,
            POJA_APPLICATION_ENVIRONMENT_ID,
            List.of(
                initiateStackDeployment(EVENT),
                initiateStackDeployment(COMPUTE_PERMISSION),
                initiateStackDeployment(STORAGE_BUCKET),
                initiateStackDeployment(STORAGE_DATABASE_POSTGRES),
                initiateStackDeployment(STORAGE_DATABASE_SQLITE)));
    assertTrue(ignoreIds(actual).contains(stackDeploymentInitiated(EVENT)));
    assertTrue(ignoreIds(actual).contains(stackDeploymentInitiated(COMPUTE_PERMISSION)));
    assertTrue(ignoreIds(actual).contains(stackDeploymentInitiated(STORAGE_BUCKET)));
    assertTrue(ignoreIds(actual).contains(stackDeploymentInitiated(STORAGE_DATABASE_POSTGRES)));
    assertTrue(ignoreIds(actual).contains(stackDeploymentInitiated(STORAGE_DATABASE_SQLITE)));
  }

    @Test
    void crupdate_applications_ok() throws ApiException {
        ApiClient joeDoeClient = anApiClient();
        ApplicationApi api = new ApplicationApi(joeDoeClient);

        List<Application> actual = api.crupdateApplications(
                List.of(
                        applicationToUpdate().archived(true),
                        applicationToCreate()
                )
        ).stream().map(ApplicationIT::ignoreIds).toList();

        assertTrue(actual.contains(updatedApplication()));
        assertTrue(actual.contains(createdApplication()));
    }

    private static List<Stack> ignoreIds(List<Stack> stacks) {
        return stacks.stream().map(stack -> stack.id(POJA_CREATED_STACK_ID)).toList();
    }

    private static Application ignoreIds(Application application) {
        return application.id(POJA_APPLICATION_ID).creationDatetime(POJA_APPLICATION_CREATION_DATETIME);
    }
}
