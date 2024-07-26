package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.GH_APP_INSTALL_1_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_ID;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static java.lang.Boolean.FALSE;
import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.GithubAppApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.AppInstallation;
import api.jcloudify.app.endpoint.rest.model.CrupdateGithubAppInstallationsRequestBody;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;
import api.jcloudify.app.service.jwt.JwtGenerator;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureMockMvc
class ApplicationInstallationIT extends FacadeIT {
  @LocalServerPort private int port;

  @MockBean GithubComponent githubComponent;
  @MockBean CloudformationComponent cloudformationComponent;
  @MockBean BucketComponent bucketComponent;
  @MockBean JwtGenerator jwtGenerator;

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
    GithubAppApi api = new GithubAppApi(joeDoeClient);
    AppInstallation toCreate =
        new AppInstallation()
            .id(randomUUID().toString())
            .isOrg(false)
            .owner("joedoe")
            .ghInstallationId(1234L);

    api.crupdateGithubAppInstallations(
        JOE_DOE_ID, new CrupdateGithubAppInstallationsRequestBody().data(List.of(toCreate)));
    AppInstallation updated = cloneAndModify(toCreate);
    var updateGithubAppInstallationResponse =
        requireNonNull(
            api.crupdateGithubAppInstallations(
                JOE_DOE_ID,
                new CrupdateGithubAppInstallationsRequestBody().data(List.of(updated))));
    List<AppInstallation> actual = requireNonNull(updateGithubAppInstallationResponse.getData());

    assertTrue(actual.contains(updated));
  }

  private static AppInstallation cloneAndModify(AppInstallation appInstallation) {
    return new AppInstallation()
        .id(appInstallation.getId())
        .isOrg(FALSE.equals(appInstallation.getIsOrg()))
        .owner(appInstallation.getOwner())
        .ghInstallationId(appInstallation.getGhInstallationId());
  }

  @Test
  void get_all_applications_ok() throws ApiException {
    ApiClient joeDoeClient = anApiClient();
    GithubAppApi api = new GithubAppApi(joeDoeClient);

    var getUserInstallationsResponse = requireNonNull(api.getUserInstallations(JOE_DOE_ID));
    var actual = requireNonNull(getUserInstallationsResponse.getData());

    assertTrue(actual.contains(appInstallation1()));
    assertFalse(actual.contains(appInstallation2()));
  }

  private static AppInstallation appInstallation1() {
    return new AppInstallation()
        .id(GH_APP_INSTALL_1_ID)
        .isOrg(false)
        .owner("joedoelogin1")
        .ghInstallationId(12344L);
  }

  private static AppInstallation appInstallation2() {
    return new AppInstallation()
        .id("gh_app_install_3_id")
        .isOrg(false)
        .owner("janedoelogin")
        .ghInstallationId(12346L);
  }
}
