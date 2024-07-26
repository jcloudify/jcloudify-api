package api.jcloudify.app.integration.conf.utils;

import static api.jcloudify.app.integration.conf.utils.TestMocks.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.file.ExtendedBucketComponent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;
import org.kohsuke.github.GHMyself;
import org.springframework.core.io.ClassPathResource;

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
    when(githubComponent.getGithubUserId(JANE_DOE_TOKEN))
        .thenReturn(Optional.of(JANE_DOE_GITHUB_ID));
    when(githubComponent.createRepoFor(any(), eq(JOE_DOE_TOKEN)))
        .thenReturn(URI.create("https://github.com/JohnDoe"));
    when(githubComponent.createRepoFor(any(), eq(JANE_DOE_TOKEN)))
        .thenReturn(URI.create("https://github.com/JaneDoe"));
    when(githubComponent.updateRepoFor(any(), eq(JOE_DOE_TOKEN), any()))
        .thenReturn(URI.create("https://github.com/JohnDoe"));
    when(githubComponent.updateRepoFor(any(), eq(JANE_DOE_TOKEN), any()))
        .thenReturn(URI.create("https://github.com/JaneDoe"));
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

  public static void setUpBucketComponent(BucketComponent bucketComponent) throws IOException {
    when(bucketComponent.presign(any(), any()))
        .thenReturn(new URL("https://example.com/templatel"));
  }

  public static void setUpExtendedBucketComponent(ExtendedBucketComponent extendedBucketComponent)
      throws IOException {
    ClassPathResource stackEventResource = new ClassPathResource("files/log.json");
    String stackEventFileBucketKey =
        String.format(
            "users/%s/apps/%s/envs/%s/stacks/%s/events/%s",
            JOE_DOE_ID,
            OTHER_POJA_APPLICATION_ID,
            OTHER_POJA_APPLICATION_ENVIRONMENT_ID,
            COMPUTE_PERM_STACK_ID,
            "log.json");
    when(extendedBucketComponent.download(stackEventFileBucketKey))
        .thenReturn(stackEventResource.getFile());
    when(extendedBucketComponent.doesExist(stackEventFileBucketKey)).thenReturn(true);
  }

  public static List<Stack> ignoreStackIdsAndDatetime(List<Stack> stacks) {
    return stacks.stream().map(TestUtils::ignoreStackIdAndDatetime).toList();
  }

  public static List<Stack> ignoreCfStackIdsAndDatetime(List<Stack> stacks) {
    return stacks.stream().map(TestUtils::ignoreCfStackIdAndDatetime).toList();
  }

  public static Stack ignoreStackIdAndDatetime(Stack stack) {
    stack.id(POJA_CREATED_STACK_ID);
    return ignoreStackDatetime(stack);
  }

  public static Stack ignoreCfStackIdAndDatetime(Stack stack) {
    stack.cfStackId(POJA_CF_STACK_ID);
    return ignoreStackDatetime(stack);
  }

  public static Stack ignoreStackDatetime(Stack stack) {
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
