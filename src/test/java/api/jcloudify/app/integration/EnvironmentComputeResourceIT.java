package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.PROD_COMPUTE_FRONTAL_FUNCTION;
import static api.jcloudify.app.integration.conf.utils.TestMocks.PROD_COMPUTE_WORKER_1_FUNCTION;
import static api.jcloudify.app.integration.conf.utils.TestMocks.PROD_COMPUTE_WORKER_2_FUNCTION;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.StackApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.ComputeStackResource;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
public class EnvironmentComputeResourceIT extends MockedThirdParties {
  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  private static ComputeStackResource computeStackResources() {
    return new ComputeStackResource()
        .id("poja_application_compute_1_resources_id")
        .environmentId(POJA_APPLICATION_ENVIRONMENT_ID)
        .frontalFunctionName(PROD_COMPUTE_FRONTAL_FUNCTION)
        .worker1FunctionName(PROD_COMPUTE_WORKER_1_FUNCTION)
        .worker2FunctionName(PROD_COMPUTE_WORKER_2_FUNCTION)
        .creationDatetime(Instant.parse("2024-07-18T10:15:30.00Z"));
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

    var computeStackResourcesPagedResponse =
        api.getComputeStackResources(
            JOE_DOE_ID, POJA_APPLICATION_ID, POJA_APPLICATION_ENVIRONMENT_ID, null, null);
    List<ComputeStackResource> computeStackResourcesResponseData =
        computeStackResourcesPagedResponse.getData();

    assertNotNull(computeStackResourcesResponseData);
    assertEquals(2, computeStackResourcesResponseData.size());
    assertEquals(computeStackResources(), computeStackResourcesResponseData.getFirst());
  }
}
