package api.jcloudify.app.integration;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.ApplicationApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.DeploymentInitiated;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.endpoint.rest.model.StackType.COMPUTE_PERMISSION;
import static api.jcloudify.app.endpoint.rest.model.StackType.EVENT;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_BUCKET;
import static api.jcloudify.app.endpoint.rest.model.StackType.STORAGE_DATABASE;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_CREATED_STACK_ID;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
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

    private static DeploymentInitiated stackDeploymentInitiated(StackType stackType) {
        return new DeploymentInitiated()
                .stackId(POJA_CREATED_STACK_ID)
                .applicationName("test-poja-app")
                .stack(stackType)
                .environmentType(PROD);
    }

    private static InitiateDeployment initiateStackDeployment(StackType stackType) {
        return new InitiateDeployment()
                .applicationName("test-poja-app")
                .stack(stackType)
                .environmentType(PROD);
    }

    private ApiClient anApiClient() {
        return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
    }

    @BeforeEach
    void setup() {
        setUpGithub(githubComponent);
        setUpCloudformationComponent(cloudformationComponent);
    }

    @Test
    void initiate_event_stack_deployment_ok() throws ApiException {
        ApiClient joeDoeClient = anApiClient();
        ApplicationApi api = new ApplicationApi(joeDoeClient);

        List<DeploymentInitiated> eventStackDeploymentInitiated = api.initiateStackDeployment(POJA_APPLICATION_ID,
                POJA_APPLICATION_ENVIRONMENT_ID,
                List.of(initiateStackDeployment(EVENT), initiateStackDeployment(COMPUTE_PERMISSION),
                        initiateStackDeployment(STORAGE_BUCKET), initiateStackDeployment(STORAGE_DATABASE)));
        assertTrue(eventStackDeploymentInitiated.contains(stackDeploymentInitiated(EVENT)));
        assertTrue(eventStackDeploymentInitiated.contains(stackDeploymentInitiated(COMPUTE_PERMISSION)));
        assertTrue(eventStackDeploymentInitiated.contains(stackDeploymentInitiated(STORAGE_BUCKET)));
        assertTrue(eventStackDeploymentInitiated.contains(stackDeploymentInitiated(STORAGE_DATABASE)));
    }
}
