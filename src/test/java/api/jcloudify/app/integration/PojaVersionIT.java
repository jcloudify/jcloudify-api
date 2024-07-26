package api.jcloudify.app.integration;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpBucketComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpCloudformationComponent;
import static api.jcloudify.app.integration.conf.utils.TestUtils.setUpGithub;
import static org.junit.jupiter.api.Assertions.assertTrue;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.conf.FacadeIT;
import api.jcloudify.app.endpoint.rest.api.PojaVersionsApi;
import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.client.ApiException;
import api.jcloudify.app.endpoint.rest.model.PojaVersion;
import api.jcloudify.app.endpoint.rest.model.PojaVersionsResponse;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.integration.conf.utils.TestUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

@Slf4j
class PojaVersionIT extends FacadeIT {

  @MockBean GithubComponent githubComponent;
  @MockBean CloudformationComponent cloudformationComponent;
  @MockBean BucketComponent bucketComponent;
  @LocalServerPort private int port;

  @BeforeEach
  void setup() throws IOException {
    setUpGithub(githubComponent);
    setUpCloudformationComponent(cloudformationComponent);
    setUpBucketComponent(bucketComponent);
  }

  private ApiClient anApiClient() {
    return TestUtils.anApiClient(JOE_DOE_TOKEN, port);
  }

  private PojaVersion pojaConf1() {
    var from = api.jcloudify.app.model.PojaVersion.POJA_1;
    return createFrom(from);
  }

  private static PojaVersion createFrom(api.jcloudify.app.model.PojaVersion from) {
    return new PojaVersion()
        .major(from.getMajor())
        .minor(from.getMinor())
        .patch(from.getPatch())
        .humanReadableValue(from.toHumanReadableValue());
  }

  @Test
  void read_all_versions_ok() throws ApiException {
    var apiClient = anApiClient();
    var api = new PojaVersionsApi(apiClient);

    PojaVersionsResponse pojaVersions = api.getPojaVersions();
    var data = Objects.requireNonNull(pojaVersions.getData());

    assertTrue(data.contains(pojaConf1()));
  }
}
