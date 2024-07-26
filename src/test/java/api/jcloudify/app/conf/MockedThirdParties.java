package api.jcloudify.app.conf;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;
import api.jcloudify.app.file.BucketComponent;
import api.jcloudify.app.service.jwt.JwtGenerator;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

public class MockedThirdParties extends FacadeIT {
  @LocalServerPort protected int port;

  @MockBean protected GithubComponent githubComponent;
  @MockBean protected CloudformationComponent cloudformationComponent;
  @MockBean protected BucketComponent bucketComponent;
  @MockBean protected JwtGenerator generator;
}
