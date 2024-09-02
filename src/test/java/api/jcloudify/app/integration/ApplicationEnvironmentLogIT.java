package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.PROD_COMPUTE_FRONTAL_FUNCTION;
import static api.jcloudify.app.integration.conf.utils.TestMocks.PROD_COMPUTE_FRONTAL_FUNCTION_LOG_GROUP;
import static api.jcloudify.app.integration.conf.utils.TestMocks.prodComputeFrontalFunctionLogGroup;
import static api.jcloudify.app.integration.conf.utils.TestMocks.prodLogGroupLogStreams;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpExtendedBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.EnvironmentApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
public class ApplicationEnvironmentLogIT extends MockedThirdParties {
  private static final Logger log = LoggerFactory.getLogger(ApplicationEnvironmentLogIT.class);
  @MockBean ExtendedBucketComponent extendedBucketComponent;

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

    String logGroupName = encode(PROD_COMPUTE_FRONTAL_FUNCTION_LOG_GROUP, UTF_8);

    var pagedResponseData =
            environmentApi.getLogStreams(
                    JOE_DOE_ID,
                    POJA_APPLICATION_ID,
                    POJA_APPLICATION_ENVIRONMENT_ID,
                    PROD_COMPUTE_FRONTAL_FUNCTION,
                    logGroupName,
                    null,
                    null);
    var logsStreamsData = pagedResponseData.getData();

    assertNotNull(logsStreamsData);
    assertTrue(logsStreamsData.containsAll(prodLogGroupLogStreams()));
  }

}