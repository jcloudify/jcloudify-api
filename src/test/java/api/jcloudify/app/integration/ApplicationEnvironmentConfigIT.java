package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.getValidPojaConf1;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joePojaApplication1;
import static api.jcloudify.app.integration.conf.utils.TestUtils.assertThrowsApiException;
import static api.jcloudify.app.integration.conf.utils.TestUtils.assertThrowsNotFoundException;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.endpoint.rest.api.EnvironmentApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@Slf4j
public class ApplicationEnvironmentConfigIT extends MockedThirdParties {
  @MockBean EventProducer<PojaConfUploaded> eventProducerMock;
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

  @Test
  void read_env_config_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new EnvironmentApi(apiClient);
    var currentApplication = joePojaApplication1();
    var currentEnv = requireNonNull(currentApplication.getEnvironments()).getFirst();
    var expected = new OneOfPojaConf(getValidPojaConf1());

    var actual =
        api.getApplicationEnvironmentConfig(
            JOE_DOE_ID, currentApplication.getId(), currentEnv.getId());

    assertEquals(expected, actual);
  }

  @Test
  void read_empty_from_db_env_config_ko() {
    var apiClient = anApiClient();
    var api = new EnvironmentApi(apiClient);

    assertThrowsApiException(
        () ->
            api.getApplicationEnvironmentConfig(
                JOE_DOE_ID, "other_poja_application_id", "other_poja_application_environment_2_id"),
        "{\"type\":\"500 INTERNAL_SERVER_ERROR\",\"message\":\"config not found in DB for user.Id ="
            + " joe_doe_id app.Id = other_poja_application_id environment.Id ="
            + " other_poja_application_environment_2_id\"}");
  }

  @Test
  void read_empty_from_s3_env_config_ko() {
    reset(extendedBucketComponentMock);
    when(extendedBucketComponentMock.doesExist(any())).thenReturn(false);
    var apiClient = anApiClient();
    var api = new EnvironmentApi(apiClient);
    var currentApplication = joePojaApplication1();
    var currentEnv = requireNonNull(currentApplication.getEnvironments()).getFirst();

    assertThrowsNotFoundException(
        () ->
            api.getApplicationEnvironmentConfig(
                JOE_DOE_ID, currentApplication.getId(), currentEnv.getId()),
        "config not found in S3 for user.Id = joe_doe_id app.Id = poja_application_id"
            + " environment.Id = poja_application_environment_id");
  }
}
