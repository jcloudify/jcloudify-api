package api.jcloudify.app.service;

import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.model.exception.ApiException;
import java.io.IOException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConf {
  @Bean
  RestTemplate restTemplate(RestTemplateBuilder builder) {
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory();
    return builder
        .requestFactory(() -> requestFactory)
        .errorHandler(new RestTemplateErrorHandler())
        .build();
  }

  private static class RestTemplateErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
      return response.getStatusCode().is4xxClientError()
          || response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
      var statusCode = response.getStatusCode();
      if (statusCode.is4xxClientError()) {
        throw new ApiException(
            SERVER_EXCEPTION, "client exception with status " + response.getStatusCode());
      } else if (statusCode.is5xxServerError()) {
        throw new ApiException(
            SERVER_EXCEPTION, "server exception with status " + response.getStatusCode());
      }
    }
  }
}
