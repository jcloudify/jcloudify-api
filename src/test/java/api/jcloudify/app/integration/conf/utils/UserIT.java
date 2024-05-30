package api.jcloudify.app.integration.conf.utils;

import static api.jcloudify.app.integration.conf.utils.TestMocks.*;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.SecurityApi;
import api.jcloudify.app.endpoint.rest.api.UserApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.CreateUser;
import api.jcloudify.app.endpoint.rest.model.User;
import api.jcloudify.app.endpoint.rest.model.Whoami;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHMyself;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@Testcontainers
@AutoConfigureMockMvc
public class UserIT extends FacadeIT {
  @MockBean GithubComponent githubComponent;

  @LocalServerPort private int port;
  private static final GHMyself johnDoe = mock(GHMyself.class);

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  private static Whoami authenticated() {
    return new Whoami().user(joeDoeUser());
  }

  @BeforeEach
  void setup() {
    setUpGithub(githubComponent, johnDoe);
  }

  @Test
  void whoami_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    SecurityApi api = new SecurityApi(joeDoeClient);

    Whoami actual = api.whoami();

    assertEquals(authenticated(), actual);
  }
}
