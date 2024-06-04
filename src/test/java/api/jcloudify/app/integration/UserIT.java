package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_AVATAR;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_GITHUB_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_USERNAME;
import static api.jcloudify.app.integration.conf.utils.TestMocks.joeDoeUser;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertEquals;

import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.SecurityApi;
import api.jcloudify.app.endpoint.rest.api.UserApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.model.User;
import api.jcloudify.app.endpoint.rest.model.Whoami;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHMyself;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
public class UserIT extends FacadeIT {
  @MockBean GithubComponent githubComponent;
  @MockBean GHMyself githubUser;

  @LocalServerPort private int port;

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  private static Whoami authenticated() {
    return new Whoami().user(joeDoeUser());
  }

  @BeforeEach
  void setup() {
    setUpGithub(githubComponent, githubUser);
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

    User actual = api.createUser(List.of(toCreate)).getFirst();

    assertEquals("test@example.com", actual.getEmail());
    assertEquals(JOE_DOE_AVATAR, actual.getAvatar());
    assertEquals(JOE_DOE_GITHUB_ID, actual.getGithubId());
    assertEquals(JOE_DOE_USERNAME, actual.getUsername());
  }
}
