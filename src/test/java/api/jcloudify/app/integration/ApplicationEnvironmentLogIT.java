package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.FIRST_LOG_STREAM_NAME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.PROD_COMPUTE_FRONTAL_FUNCTION;
import static api.jcloudify.app.integration.conf.utils.TestMocks.PROD_COMPUTE_FRONTAL_FUNCTION_LOG_GROUP;
import static api.jcloudify.app.integration.conf.utils.TestMocks.prodComputeFrontalFunctionLogGroup;
import static api.jcloudify.app.integration.conf.utils.TestMocks.prodLogGroupLogStreams;
import static api.jcloudify.app.integration.conf.utils.TestMocks.prodFirstLogStreamEvents;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpExtendedBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.rest.api.EnvironmentApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
public class ApplicationEnvironmentLogIT extends MockedThirdParties {
  @MockBean ExtendedBucketComponent extendedBucketComponent;
  @MockBean EventProducer eventProducer;

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  @BeforeEach
  void setup() throws IOException, URISyntaxException {
    setUpGithub(githubComponent);
    setUpCloudformationComponent(cloudformationComponent);
    setUpBucketComponent(bucketComponent);
    setUpExtendedBucketComponent(extendedBucketComponent);
  }

  @Test
  void get_log_groups_ok() throws ApiException {
    ApiClient joeDoeApiClient = anApiClient();
    EnvironmentApi environmentApi = new EnvironmentApi(joeDoeApiClient);

    var pagedResponseData =
        environmentApi.getFunctionLogGroups(
            JOE_DOE_ID,
            POJA_APPLICATION_ID,
            POJA_APPLICATION_ENVIRONMENT_ID,
            PROD_COMPUTE_FRONTAL_FUNCTION,
            null,
            null);
    var logGroupsData = pagedResponseData.getData();

    assertNotNull(logGroupsData);
    assertTrue(logGroupsData.contains(prodComputeFrontalFunctionLogGroup()));
  }

  @Test
  void get_log_streams_ok() throws ApiException {
    ApiClient joeDoeApiClient = anApiClient();
    EnvironmentApi environmentApi = new EnvironmentApi(joeDoeApiClient);

    var pagedResponseData =
        environmentApi.getFunctionLogStreams(
            JOE_DOE_ID,
            POJA_APPLICATION_ID,
            POJA_APPLICATION_ENVIRONMENT_ID,
            PROD_COMPUTE_FRONTAL_FUNCTION,
            PROD_COMPUTE_FRONTAL_FUNCTION_LOG_GROUP,
            null,
            null);
    var logsStreamsData = pagedResponseData.getData();

    assertNotNull(logsStreamsData);
    assertTrue(logsStreamsData.containsAll(prodLogGroupLogStreams()));
  }

  @Test
  void get_log_stream_events_ok() throws ApiException {
    ApiClient joeDoeApiClient = anApiClient();
    EnvironmentApi environmentApi = new EnvironmentApi(joeDoeApiClient);

    var pagedResponseData =
            environmentApi.getFunctionLogStreamEvents(
                    JOE_DOE_ID,
                    POJA_APPLICATION_ID,
                    POJA_APPLICATION_ENVIRONMENT_ID,
                    PROD_COMPUTE_FRONTAL_FUNCTION,
                    PROD_COMPUTE_FRONTAL_FUNCTION_LOG_GROUP,
                    FIRST_LOG_STREAM_NAME,
                    null,
                    null);
    var logsStreamEventsData = pagedResponseData.getData();
    assertNotNull(logsStreamEventsData);
    assertTrue(logsStreamEventsData.containsAll(prodFirstLogStreamEvents()));
  }
}
