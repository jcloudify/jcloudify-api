package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JANE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestUtils.assertThrowsForbiddenException;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.HealthApi;
import api.jcloudify.app.endpoint.rest.api.SecurityApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BetaTestEndpointIT extends MockedThirdParties {

  private ApiClient anApiClient(String token) {
    return TestUtils.anApiClient(token, port);
  }

  @BeforeEach
  void setup() {
    setUpGithub(githubComponent);
  }

  @Test
  void non_beta_tester_whoami_ko() {
    ApiClient janeDoeClient = anApiClient(JANE_DOE_TOKEN);
    SecurityApi api = new SecurityApi(janeDoeClient);
    assertThrowsForbiddenException(api::whoami, "Access Denied");
  }

  @Test
  void non_beta_tester_beta_ping_ko() {
    ApiClient janeDoeClient = anApiClient(JANE_DOE_TOKEN);
    HealthApi api = new HealthApi(janeDoeClient);
    assertThrowsForbiddenException(api::betaPing, "Access Denied");
  }

  @Test
  void beta_tester_beta_ping_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient(JOE_DOE_TOKEN);
    HealthApi api = new HealthApi(joeDoeClient);

    String actual = api.betaPing();

    assertEquals("beta-pong", actual);
  }
}
