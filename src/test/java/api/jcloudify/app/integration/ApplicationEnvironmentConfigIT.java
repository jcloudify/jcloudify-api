package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joePojaApplication1;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static api.jcloudify.app.model.PojaVersion.POJA_V16_2_1;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.EnvironmentApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConfV1621;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.net.MalformedURLException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@Slf4j
public class ApplicationEnvironmentConfigIT extends FacadeIT {
  @LocalServerPort private int port;

  @MockBean GithubComponent githubComponent;
  @MockBean CloudformationComponent cloudformationComponent;
  @MockBean BucketComponent bucketComponent;

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
  void update_environment_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new EnvironmentApi(apiClient);
    var currentApplication = joePojaApplication1();
    var currentEnv = requireNonNull(currentApplication.getEnvironments()).getFirst();
    var payload = new OneOfPojaConf(pojaConfV1331());

    var actual =
        api.configureApplicationEnv(
            JOE_DOE_ID, currentApplication.getId(), currentEnv.getId(), payload);

    assertEquals(payload, actual);
  }

  private static @NotNull PojaConfV1621 pojaConfV1331() {
    return new PojaConfV1621().version(POJA_V16_2_1.toHumanReadableValue());
  }
}
