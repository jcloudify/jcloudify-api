package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.GH_APP_INSTALL_1_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestUtils.APP_INSTALLATION_1_ID;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.conf.MockedThirdParties;
import api.jcloudify.app.endpoint.rest.api.GithubAppInstallationApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.CreateGithubAppInstallation;
import api.jcloudify.app.endpoint.rest.model.CrupdateGithubAppInstallationsRequestBody;
import api.jcloudify.app.endpoint.rest.model.GithubAppInstallation;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
class ApplicationInstallationIT extends MockedThirdParties {
  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  @BeforeEach
  void setup() throws IOException {
    setUpGithub(githubComponent);
    setUpCloudformationComponent(cloudformationComponent);
    setUpBucketComponent(bucketComponent);
  }

  @Test
  void crupdate_applications_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    GithubAppInstallationApi api = new GithubAppInstallationApi(joeDoeClient);
    CreateGithubAppInstallation toCreate =
        new CreateGithubAppInstallation()
            .id(randomUUID().toString())
            .ghInstallationId(APP_INSTALLATION_1_ID);

    var createGithubAppInstallationResponse =
        api.crupdateGithubAppInstallations(
            JOE_DOE_ID, new CrupdateGithubAppInstallationsRequestBody().data(List.of(toCreate)));

    List<GithubAppInstallation> actual =
        requireNonNull(createGithubAppInstallationResponse.getData());
    GithubAppInstallation first = actual.getFirst();

    assertEquals(toCreate.getId(), first.getId());
    assertEquals(toCreate.getGhInstallationId(), first.getGhInstallationId());
  }

  @Test
  void get_all_applications_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    GithubAppInstallationApi api = new GithubAppInstallationApi(joeDoeClient);

    var getUserInstallationsResponse = requireNonNull(api.getUserInstallations(JOE_DOE_ID));
    var actual = requireNonNull(getUserInstallationsResponse.getData());

    assertTrue(actual.contains(appInstallation1()));
    assertFalse(actual.contains(appInstallation2()));
  }

  private static GithubAppInstallation appInstallation1() {
    return new GithubAppInstallation()
        .id(GH_APP_INSTALL_1_ID)
        .type("User")
        .ghAvatarUrl("http://testimage.com")
        .owner("joedoelogin1")
        .ghInstallationId(12344L);
  }

  private static GithubAppInstallation appInstallation2() {
    return new GithubAppInstallation()
        .id("gh_app_install_2_id")
        .type("Organization")
        .ghAvatarUrl("http://testimage.com")
        .owner("janedoelogin")
        .ghInstallationId(12346L);
  }
}
