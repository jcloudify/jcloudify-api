package api.jcloudify.app.integration.conf.utils;

import static api.jcloudify.app.integration.conf.utils.TestMocks.*;
import static org.mockito.Mockito.when;

import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import java.util.Optional;

public class TestUtils {
  public static ApiClient anApiClient(String token, int serverPort) {
    ApiClient client = new ApiClient();
    client.setScheme("http");
    client.setHost("localhost");
    client.setPort(serverPort);
    client.setRequestInterceptor(
        httpRequestBuilder -> httpRequestBuilder.header("Authorization", "Bearer " + token));
    return client;
  }

  public static void setUpGithub(GithubComponent githubComponent) {
    when(githubComponent.getEmailByToken(JOE_DOE_TOKEN)).thenReturn(Optional.of(JOE_DOE_EMAIL));
  }
}
