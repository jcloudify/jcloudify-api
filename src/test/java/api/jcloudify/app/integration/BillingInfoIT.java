package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.BILLING_INFO_END_TIME_QUERY;
import static api.jcloudify.app.integration.conf.utils.TestMocks.BILLING_INFO_START_TIME_QUERY;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.OTHER_POJA_APPLICATION_ENVIRONMENT_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.OTHER_POJA_APPLICATION_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joeDoeBillingInfo1;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joeDoeBillingInfo2;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joeDoeTotalBillingInfo;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.BillingApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
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
public class BillingInfoIT extends MockedThirdParties {
  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  @BeforeEach
  void setup() throws IOException {
    setUpGithub(githubComponent);
  }

  @Test
  void get_user_billing_info_by_env_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    BillingApi api = new BillingApi(joeDoeClient);

    var actual =
        api.getUserAppEnvironmentBillingInfo(
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
            BILLING_INFO_START_TIME_QUERY,
            BILLING_INFO_END_TIME_QUERY);

    assertEquals(joeDoeBillingInfo1(), actual);
  }

  @Test
  void get_user_billing_info_by_app_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    BillingApi api = new BillingApi(joeDoeClient);

    var actual =
        api.getUserApplicationBillingInfo(
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            BILLING_INFO_START_TIME_QUERY,
            BILLING_INFO_END_TIME_QUERY);

    assertTrue(actual.containsAll(List.of(joeDoeBillingInfo1(), joeDoeBillingInfo2())));
  }

  @Test
  void get_user_total_billing_info() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    BillingApi api = new BillingApi(joeDoeClient);

    var actual =
        api.getUserBillingInfo(
            JOE_DOE_ID, BILLING_INFO_START_TIME_QUERY, BILLING_INFO_END_TIME_QUERY);

    assertEquals(joeDoeTotalBillingInfo(), actual);
  }

  @Test
  void get_user_empty_billing_info() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    BillingApi api = new BillingApi(joeDoeClient);

    var actual =
        api.getUserBillingInfo(
            JOE_DOE_ID,
            Instant.parse("2024-01-01T00:00:00.00Z"),
            Instant.parse("2024-01-02T00:00:00.00Z"));

    assertEquals(ZERO, actual.getComputedPrice());
  }
}
