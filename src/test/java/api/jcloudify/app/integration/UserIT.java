package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.*;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpStripe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
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

  private static Whoami joeDoeWhoami() {
    return new Whoami().user(joeDoeUser());
  }

  @BeforeEach
  void setup() {
    setUpStripe(stripeService);
  }

  @Test
  void whoami_ok() throws ApiException {
    setUpGithub(githubComponent, githubUser);
    ApiClient joeDoeClient = anApiClient();
    SecurityApi api = new SecurityApi(joeDoeClient);

    Whoami actual = api.whoami();

    assertEquals(joeDoeWhoami(), actual);
  }

  @Test
  void signup_ok() throws ApiException {
    when(githubComponent.getGithubUserId(NEW_USER_TOKEN))
        .thenReturn(Optional.of(NEW_USER_GITHUB_ID));
    when(githubComponent.getCurrentUserByToken(NEW_USER_TOKEN)).thenReturn(Optional.of(githubUser));
    when(githubUser.getLogin()).thenReturn(JOE_DOE_USERNAME);
    when(githubUser.getId()).thenReturn(Long.valueOf(NEW_USER_GITHUB_ID));
    when(githubUser.getAvatarUrl()).thenReturn(JOE_DOE_AVATAR);
    ApiClient joeDoeClient = anApiClient();
    UserApi api = new UserApi(joeDoeClient);

    CreateUser toCreate =
        new CreateUser()
            .firstName("firstName")
            .lastName("lastName")
            .email("test@example.com")
            .token(NEW_USER_TOKEN);

    User actual =
        Objects.requireNonNull(
                api.createUser(new CreateUsersRequestBody().data(List.of(toCreate))).getData())
            .getFirst();

    assertEquals("test@example.com", actual.getEmail());
    assertEquals(JOE_DOE_AVATAR, actual.getAvatar());
    assertEquals(NEW_USER_GITHUB_ID, actual.getGithubId());
    assertEquals(JOE_DOE_USERNAME, actual.getUsername());
    assertEquals(JOE_DOE_STRIPE_ID, actual.getStripeId());
  }
}
