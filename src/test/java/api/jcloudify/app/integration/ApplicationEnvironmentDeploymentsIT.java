package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PREPROD;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.getValidPojaConf1;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.ApplicationApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;

class ApplicationEnvironmentDeploymentsIT extends MockedThirdParties {
  @MockBean ExtendedBucketComponent extendedBucketComponentMock;

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  @BeforeEach
  void setup() throws IOException {
    setUpGithub(githubComponent);
    setUpCloudformationComponent(cloudformationComponent);
    setUpBucketComponent(bucketComponent);
    setUpExtendedBucketComponentMock(extendedBucketComponentMock);
  }

  void setUpExtendedBucketComponentMock(ExtendedBucketComponent extendedBucketComponent)
      throws IOException {
    when(extendedBucketComponent.doesExist(any())).thenReturn(true);
    when(extendedBucketComponent.download(any()))
        .thenReturn(new ClassPathResource("files/poja_1.yml").getFile());
  }

  @Test
  void read_deployments_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new ApplicationApi(apiClient);

    var prodDepls =
        api.getApplicationDeployments(JOE_DOE_ID, POJA_APPLICATION_ID, PROD, null, null, 1, 10);
    var preprodDepls =
        api.getApplicationDeployments(JOE_DOE_ID, POJA_APPLICATION_ID, PREPROD, null, null, 1, 10);
    var allDepls =
        api.getApplicationDeployments(JOE_DOE_ID, POJA_APPLICATION_ID, null, null, null, 1, 10);
    var instantFilteredDepls =
        api.getApplicationDeployments(
            JOE_DOE_ID, POJA_APPLICATION_ID, null, Instant.parse(""), Instant.parse(""), 1, 10);
  }

  @Test
  void read_deployment_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new ApplicationApi(apiClient);

    var actual = api.getApplicationDeployment(JOE_DOE_ID, POJA_APPLICATION_ID, "deployment_1_id");

    assertEquals(null, actual);
  }

  @Test
  void read_env_config_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new ApplicationApi(apiClient);
    var expected = new OneOfPojaConf(getValidPojaConf1());

    var actual =
        api.getApplicationDeploymentConfig(JOE_DOE_ID, POJA_APPLICATION_ID, "deployment_1_id");
    assertEquals(expected, actual);
  }
}
