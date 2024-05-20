package api.jcloudify.app.integration.conf.utils;

import api.jcloudify.app.endpoint.rest.client.ApiClient;
import api.jcloudify.app.endpoint.rest.security.github.GithubComponent;

import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_EMAIL;
import static api.jcloudify.app.integration.conf.utils.TestMocks.JOE_DOE_TOKEN;
import static org.mockito.Mockito.when;

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
        when(githubComponent.getEmailByToken(JOE_DOE_TOKEN)).thenReturn(JOE_DOE_EMAIL);
    }
}
