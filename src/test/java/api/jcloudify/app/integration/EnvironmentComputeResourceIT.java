package api.jcloudify.app.integration;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.StackApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.ComputeStackResources;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.List;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@AutoConfigureMockMvc
public class EnvironmentComputeResourceIT extends MockedThirdParties {
    private ApiClient anApiClient() {
        return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
    }

    private static ComputeStackResources computeStackResources() {
        return new ComputeStackResources()
                .id("poja_application_compute_resources_id")
                .environmentId(POJA_APPLICATION_ENVIRONMENT_ID)
                .frontalFunctionName("prod-compute-frontal-function")
                .worker1FunctionName("prod-compute-worker-1-function")
                .worker2FunctionName("prod-compute-worker-2-function");
    }

    @BeforeEach
    void setup() throws IOException {
        setUpGithub(githubComponent);
        setUpCloudformationComponent(cloudformationComponent);
        setUpBucketComponent(bucketComponent);
    }

    @Test
    void get_environment_compute_stack_resources() throws ApiException {
        ApiClient joeDoeClient = anApiClient();
        StackApi api = new StackApi(joeDoeClient);

        var computeStackResourcesPagedResponse = api.getComputeStackResources(JOE_DOE_ID, POJA_APPLICATION_ID, POJA_APPLICATION_ENVIRONMENT_ID, null, null);
        List<ComputeStackResources> computeStackResourcesResponseData = computeStackResourcesPagedResponse.getData();

        assertNotNull(computeStackResourcesResponseData);
        assertEquals(1, computeStackResourcesResponseData.size());
        assertEquals(computeStackResources(), computeStackResourcesResponseData.getFirst());
    }
}
