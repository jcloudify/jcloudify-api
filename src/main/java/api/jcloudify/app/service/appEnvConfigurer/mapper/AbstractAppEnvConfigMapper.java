package api.jcloudify.app.service.appEnvConfigurer.mapper;

import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.service.appEnvConfigurer.NetworkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;

abstract sealed class AbstractAppEnvConfigMapper implements PojaConfFileMapper
    permits AppEnvConfigMapperFacade, PojaConfV17_0_0Mapper {
  protected final ObjectMapper yamlObjectMapper;
  protected final NetworkingService networkingService;

  AbstractAppEnvConfigMapper(
      @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper,
      NetworkingService networkingService) {
    this.yamlObjectMapper = yamlObjectMapper;
    this.networkingService = networkingService;
  }

  protected final File createNamedTempFile(String filename) {
    return new File(createTempDir(), filename);
  }

  protected abstract File writeToTempFile(PojaConf pojaConf);

  @SneakyThrows
  private static File createTempDir() {
    return Files.createTempDirectory("poja-conf").toFile();
  }
}
