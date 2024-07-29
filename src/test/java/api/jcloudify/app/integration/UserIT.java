package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_AVATAR;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_GITHUB_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_STRIPE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_USERNAME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joeDoeUser;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpStripe;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.SecurityApi;
import api.jcloudify.app.endpoint.rest.api.UserApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.model.CreateUsersRequestBody;
import api.jcloudify.app.endpoint.rest.model.User;
import api.jcloudify.app.endpoint.rest.model.Whoami;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import api.jcloudify.app.service.StripeService;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHMyself;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
class UserIT extends MockedThirdParties {
  @MockBean GHMyself githubUser;
  @MockBean StripeService stripeService;

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  private static Whoami authenticated() {
    return new Whoami().user(joeDoeUser());
  }

  @BeforeEach
  void setup() {
    setUpGithub(githubComponent, githubUser);
    setUpStripe(stripeService);
  }

  @Test
  void whoami_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    SecurityApi api = new SecurityApi(joeDoeClient);

    Whoami actual = api.whoami();

    assertEquals(authenticated(), actual);
  }

  @Test
  void signup_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    UserApi api = new UserApi(joeDoeClient);

    CreateUser toCreate =
        new CreateUser()
            .firstName("firstName")
            .lastName("lastName")
            .email("test@example.com")
            .token(JOE_DOE_TOKEN);

    User actual =
        Objects.requireNonNull(
                api.createUser(new CreateUsersRequestBody().data(List.of(toCreate))).getData())
            .getFirst();

    assertEquals("test@example.com", actual.getEmail());
    assertEquals(JOE_DOE_AVATAR, actual.getAvatar());
    assertEquals(JOE_DOE_GITHUB_ID, actual.getGithubId());
    assertEquals(JOE_DOE_USERNAME, actual.getUsername());
    assertEquals(JOE_DOE_STRIPE_ID, actual.getStripeId());
  }
}
