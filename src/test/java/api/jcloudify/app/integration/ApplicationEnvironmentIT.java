package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.Environment.StateEnum.UNKNOWN;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.pojaAppProdEnvironment;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.ApplicationApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironment;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentsRequestBody;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.net.MalformedURLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class ApplicationEnvironmentIT extends FacadeIT {
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
  void list_environments_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    ApplicationApi api = new ApplicationApi(joeDoeClient);

    var actual = api.getApplicationEnvironments(POJA_APPLICATION_ID);
    var actualData = requireNonNull(actual.getData());

    assertTrue(actualData.contains(pojaAppProdEnvironment()));
  }

  @Test
  void crupdate_environments_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    ApplicationApi api = new ApplicationApi(joeDoeClient);
    Environment toCreate = toCreateEnv();

    var createApplicationResponse =
        api.crupdateApplicationEnvironments(
            POJA_APPLICATION_ID,
            new CrupdateEnvironmentsRequestBody().data(List.of(toCrupdateEnvironment(toCreate))));
    var updatedPayload =
        requireNonNull(createApplicationResponse.getData()).getFirst().archived(true);
    var updateApplicationResponse =
        api.crupdateApplicationEnvironments(
            POJA_APPLICATION_ID,
            new CrupdateEnvironmentsRequestBody()
                .data(List.of(toCrupdateEnvironment(updatedPayload))));

    var updateApplicationResponseData = requireNonNull(updateApplicationResponse.getData());
    assertTrue(updateApplicationResponseData.contains(toCreate));
  }

  private static Environment toCreateEnv() {
    return new Environment().id(randomUUID().toString()).environmentType(PROD).state(UNKNOWN);
  }

  private static CrupdateEnvironment toCrupdateEnvironment(Environment environment) {
    return new CrupdateEnvironment()
        .id(environment.getId())
        .environmentType(environment.getEnvironmentType())
        .archived(environment.getArchived());
  }
}
