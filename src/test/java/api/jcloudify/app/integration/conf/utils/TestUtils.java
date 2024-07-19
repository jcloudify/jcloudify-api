package api.jcloudify.app.integration.conf.utils;

import static api.jcloudify.app.integration.conf.utils.TestMocks.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;
import org.kohsuke.github.GHMyself;

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
    when(githubComponent.getGithubUserId(JOE_DOE_TOKEN)).thenReturn(Optional.of(JOE_DOE_GITHUB_ID));
  }

  @SneakyThrows
  public static void setUpGithub(GithubComponent githubComponent, GHMyself githubUser) {
    when(githubComponent.getGithubUserId(JOE_DOE_TOKEN)).thenReturn(Optional.of(JOE_DOE_GITHUB_ID));
    when(githubComponent.getCurrentUserByToken(JOE_DOE_TOKEN)).thenReturn(Optional.of(githubUser));
    when(githubUser.getEmail()).thenReturn("test@example.com");
    when(githubUser.getLogin()).thenReturn(JOE_DOE_USERNAME);
    when(githubUser.getId()).thenReturn(Long.valueOf(JOE_DOE_GITHUB_ID));
    when(githubUser.getAvatarUrl()).thenReturn(JOE_DOE_AVATAR);
  }

  public static void setUpCloudformationComponent(CloudformationComponent cloudformationComponent) {
    when(cloudformationComponent.createStack(any(), any(), any(), any()))
        .thenReturn(POJA_CF_STACK_ID);
    when(cloudformationComponent.updateStack(any(), any(), any(), any()))
        .thenReturn(POJA_CF_STACK_ID);
  }

  public static void setUpBucketComponent(BucketComponent bucketComponent)
      throws MalformedURLException {
    when(bucketComponent.presign(any(), any()))
        .thenReturn(new URL("https://example.com/templatel"));
  }

  public static List<Stack> ignoreStacksIdAndDatetime(List<Stack> stacks) {
    return stacks.stream()
            .map(TestUtils::ignoreStackIdAndDatetime)
            .toList();
  }

  public static Stack ignoreStackIdAndDatetime(Stack stack) {
    stack.id(POJA_CREATED_STACK_ID);
    stack.creationDatetime(null);
    stack.updateDatetime(null);
    return stack;
  }

  public static void assertThrowsForbiddenException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(
            "{" + "\"type\":\"403 FORBIDDEN\"," + "\"message\":\"" + message + "\"}", responseBody);
  }

  public static void assertThrowsBadRequestException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(
            "{" + "\"type\":\"400 BAD_REQUEST\"," + "\"message\":\"" + message + "\"}", responseBody);
  }
}
