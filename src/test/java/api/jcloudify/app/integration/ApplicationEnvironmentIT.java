package api.jcloudify.app.integration;

import static api.jcloudify.app.endpoint.rest.model.Environment.StateEnum.UNKNOWN;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PREPROD;
import static api.jcloudify.app.endpoint.rest.model.EnvironmentType.PROD;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.pojaAppProdEnvironment;
import static api.jcloudify.app.integration.conf.utils.TestUtils.assertThrowsBadRequestException;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.EnvironmentApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironment;
import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentsRequestBody;
import api.jcloudify.app.endpoint.rest.model.Environment;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
@Slf4j
class ApplicationEnvironmentIT extends MockedThirdParties {
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
  void list_environments_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    EnvironmentApi api = new EnvironmentApi(joeDoeClient);

    var actual = api.getApplicationEnvironments(JOE_DOE_ID, POJA_APPLICATION_ID);
    var actualData = requireNonNull(actual.getData());

    assertTrue(actualData.contains(pojaAppProdEnvironment()));
  }

  @Test
  void crupdate_environments_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    EnvironmentApi api = new EnvironmentApi(joeDoeClient);
    Environment toCreate = toCreateEnv();

    var createApplicationResponse =
        api.crupdateApplicationEnvironments(
            JOE_DOE_ID,
            POJA_APPLICATION_ID,
            new CrupdateEnvironmentsRequestBody().data(List.of(toCrupdateEnvironment(toCreate))));
    var updatedPayload =
        requireNonNull(createApplicationResponse.getData()).getFirst().archived(true);
    var updateApplicationResponse =
        api.crupdateApplicationEnvironments(
            JOE_DOE_ID,
            POJA_APPLICATION_ID,
            new CrupdateEnvironmentsRequestBody()
                .data(List.of(toCrupdateEnvironment(updatedPayload))));

    var updateApplicationResponseData = requireNonNull(updateApplicationResponse.getData());
    assertTrue(updateApplicationResponseData.contains(toCreate));
  }

  @Test
  void crupdate_environment_ko() {
    ApiClient joeDoeClient = anApiClient();
    EnvironmentApi api = new EnvironmentApi(joeDoeClient);
    Environment toCreate =
        new Environment().id(randomUUID().toString()).environmentType(PROD).state(UNKNOWN);

    assertThrowsBadRequestException(
        () ->
            api.crupdateApplicationEnvironments(
                JOE_DOE_ID,
                POJA_APPLICATION_ID,
                new CrupdateEnvironmentsRequestBody()
                    .data(List.of(toCrupdateEnvironment(toCreate)))),
        "Only one PROD environment can be created.");
  }

  @Test
  void get_envionment_by_id_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    EnvironmentApi api = new EnvironmentApi(joeDoeClient);

    Environment actual =
        api.getApplicationEnvironmentById(
            JOE_DOE_ID, POJA_APPLICATION_ID, POJA_APPLICATION_ENVIRONMENT_ID);

    assertEquals(pojaAppProdEnvironment(), actual);
  }

  @Test
  void get_envionment_by_id_ko() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    EnvironmentApi api = new EnvironmentApi(joeDoeClient);

    assertThrowsBadRequestException(
        () -> api.getApplicationEnvironmentById(JOE_DOE_ID, POJA_APPLICATION_ID, "dummy"),
        "Environment identified by id dummy for application "
            + POJA_APPLICATION_ID
            + " of user "
            + JOE_DOE_ID
            + " not found");
  }

  private static Environment toCreateEnv() {
    return new Environment().id(randomUUID().toString()).environmentType(PREPROD).state(UNKNOWN);
  }

  private static CrupdateEnvironment toCrupdateEnvironment(Environment environment) {
    return new CrupdateEnvironment()
        .id(environment.getId())
        .environmentType(environment.getEnvironmentType())
        .archived(environment.getArchived());
  }
}
