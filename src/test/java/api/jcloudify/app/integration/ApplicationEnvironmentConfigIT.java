package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.getValidPojaConf1;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joePojaApplication1;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.endpoint.rest.api.EnvironmentApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@Slf4j
public class ApplicationEnvironmentConfigIT extends MockedThirdParties {
  @MockBean EventProducer<PojaConfUploaded> eventProducer;

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
  void configure_environment_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new EnvironmentApi(apiClient);
    var currentApplication = joePojaApplication1();
    var currentEnv = requireNonNull(currentApplication.getEnvironments()).getFirst();
    var payload = new OneOfPojaConf(getValidPojaConf1());

    var actual =
        api.configureApplicationEnv(
            JOE_DOE_ID, currentApplication.getId(), currentEnv.getId(), payload);

    assertEquals(payload, actual);
  }
}
