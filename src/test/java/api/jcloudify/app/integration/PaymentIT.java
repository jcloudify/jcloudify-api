package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.paymentMethod;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpStripe;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.PaymentApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.PaymentMethod;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import api.jcloudify.app.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
public class PaymentIT extends FacadeIT {
  @LocalServerPort private int port;

  @MockBean GithubComponent githubComponent;
  @MockBean StripeService stripeService;

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  @BeforeEach
  void setup() {
    setUpGithub(githubComponent);
    setUpStripe(stripeService);
  }

  @Test
  void get_payment_methods_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    PaymentApi api = new PaymentApi(joeDoeClient);

    var paymentMethodsResponse = api.getPaymentMethods(JOE_DOE_ID);
    PaymentMethod paymentMethod = requireNonNull(paymentMethodsResponse.getData()).getFirst();

    assertEquals(paymentMethod().getId(), paymentMethod.getId());
    assertEquals(paymentMethod().getCard().getBrand(), paymentMethod.getBrand());
    assertEquals(paymentMethod().getCard().getLast4(), paymentMethod.getLast4());
    assertEquals(paymentMethod().getType(), paymentMethod.getType());
  }
}
