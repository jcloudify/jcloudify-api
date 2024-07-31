package api.jcloudify.app.service.api.pojaSam;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpMethod.PUT;

import api.jcloudify.app.file.FileWriter;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.service.api.pojaSam.model.CodeUri;
import java.io.File;
import java.net.URI;
import java.util.zip.ZipFile;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class PojaSamApi {
  public static final String X_API_KEY_HEADER_NAME = "x-api-key";
  private final RestTemplate restTemplate;
  private final FileWriter fileWriter;
  private final String apiKey;

  public PojaSamApi(
      RestTemplate restTemplate,
      FileWriter fileWriter,
      @Value("${poja.sam.api.key}") String apiKey) {
    this.restTemplate = restTemplate;
    this.fileWriter = fileWriter;
    this.apiKey = apiKey;
  }

  private CodeUri generateFromApi(PojaVersion pojaVersion, File pojaConfFile) {
    HttpHeaders headers = getPojaSamApiHttpHeaders();
    MultipartBodyBuilder bodies = new MultipartBodyBuilder();
    bodies.part("conf_file", generatePojaConf(pojaConfFile));
    MultiValueMap<String, HttpEntity<?>> multipartBody = bodies.build();
    HttpEntity<MultiValueMap<String, HttpEntity<?>>> request =
        new HttpEntity<>(multipartBody, headers);
    URI formattedSamUri = getFormattedSamUri(pojaVersion);

    log.info("downloading code from {}", formattedSamUri);
    return restTemplate.exchange(formattedSamUri, PUT, request, CodeUri.class).getBody();
  }

  private HttpHeaders getPojaSamApiHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(X_API_KEY_HEADER_NAME, apiKey);
    return headers;
  }

  private static URI getFormattedSamUri(PojaVersion pojaVersion) {
    return UriComponentsBuilder.fromUri(pojaVersion.getSamUri()).path("/gen").build().toUri();
  }

  public ZipFile genCodeTo(PojaVersion pojaVersion, File pojaConfFile) {
    var codeUri = generateFromApi(pojaVersion, pojaConfFile);
    return downloadAsZipFile(randomUUID().toString(), codeUri.uri());
  }

  private Resource generatePojaConf(File file) {
    return new FileSystemResource(file);
  }

  @SneakyThrows
  private ZipFile downloadAsZipFile(String filename, URI uri) {
    log.info("GET API CALL downloading {} from {}", filename, uri);
    var response = restTemplate.getForObject(uri, byte[].class);
    return new ZipFile(fileWriter.apply(response, null));
  }
}
