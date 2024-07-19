package api.jcloudify.app.service.appEnvConfigurer.writer;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;

abstract sealed class AbstractAppEnvConfigWriter implements PojaConfFileWriter
    permits AppEnvConfigWriterFacade, PojaConfV13_3_1Writer {
  protected final ObjectMapper yamlObjectMapper;

  AbstractAppEnvConfigWriter(@Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
    this.yamlObjectMapper = yamlObjectMapper;
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
