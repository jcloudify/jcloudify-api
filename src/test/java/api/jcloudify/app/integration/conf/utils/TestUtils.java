package api.jcloudify.app.integration.conf.utils;

import static api.jcloudify.app.integration.conf.utils.TestMocks.*;
import static api.jcloudify.app.integration.conf.utils.TestMocks.ssmParameterToCreate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.aws.ssm.SsmComponent;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.Stack;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.service.StripeService;
import api.jcloudify.app.service.github.model.GhAppInstallation;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.function.Executable;
import org.kohsuke.github.GHMyself;
import org.springframework.core.io.ClassPathResource;

public class TestUtils {

  public static final long APP_INSTALLATION_1_ID = 12344;
  public static final GhAppInstallation GH_APP_JOE_DOE_INSTALLATION_1 =
      new GhAppInstallation(APP_INSTALLATION_1_ID, "joeDoe", "User", "http://testimage.com");
  public static final long APP_INSTALLATION_2_ID = 12346;
  public static final GhAppInstallation GH_APP_JOE_DOE_INSTALLATION_2 =
      new GhAppInstallation(APP_INSTALLATION_2_ID, "joeDoe", "User", "http://testimage.com");

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
    Set<GhAppInstallation> t = ghApps();
    when(githubComponent.listInstallations()).thenReturn(t);
    when(githubComponent.getInstallationById(APP_INSTALLATION_1_ID))
        .thenReturn(GH_APP_JOE_DOE_INSTALLATION_1);
    when(githubComponent.getInstallationById(APP_INSTALLATION_2_ID))
        .thenReturn(GH_APP_JOE_DOE_INSTALLATION_2);
  }

  private static Set<GhAppInstallation> ghApps() {
    return Set.of(GH_APP_JOE_DOE_INSTALLATION_1, GH_APP_JOE_DOE_INSTALLATION_2);
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

  public static void setUpSsmComponent(SsmComponent ssmComponent) {
    when(ssmComponent.createSsmParameters(eq(List.of(ssmParameterToCreate())), any()))
        .thenReturn(List.of(awsSsmParameterModelToCreate()));
    when(ssmComponent.updateSsmParameters(eq(List.of(ssmParam1Updated()))))
        .thenReturn(List.of(awsSsmParameterModelToUpdate()));
    when(ssmComponent.getSsmParametersByNames(
            eq(List.of("/poja/prod/ssm/param1", "/poja/prod/ssm/param2"))))
        .thenReturn(
            List.of(
                ssmParameter("/poja/prod/ssm/param1", "dummy"),
                ssmParameter("/poja/prod/ssm/param2", "dummy")));
  }

  @SneakyThrows
  public static void setUpStripe(StripeService stripeService) {
    when(stripeService.createCustomer(any(), any())).thenReturn(stripeCustomer());
    when(stripeService.getPaymentMethods(any())).thenReturn(paymentMethods());
    when(stripeService.setDefaultPaymentMethod(any(), any())).thenReturn(paymentMethod());
    when(stripeService.detachPaymentMethod(any())).thenReturn(paymentMethod());
    when(stripeService.attachPaymentMethod(any(), any())).thenReturn(paymentMethod());
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

  public static void assertThrowsBadRequestException(String expectedBody, Executable executable) {
    BadRequestException badRequestException = assertThrows(BadRequestException.class, executable);
    assertEquals(expectedBody, badRequestException.getMessage());
  }

  public static void assertThrowsNotFoundException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(
        "{" + "\"type\":\"404 NOT_FOUND\"," + "\"message\":\"" + message + "\"}", responseBody);
  }

  public static void assertThrowsApiException(Executable executable, String message) {
    ApiException apiException = assertThrows(ApiException.class, executable);
    String responseBody = apiException.getResponseBody();
    assertEquals(message, responseBody);
  }
}
