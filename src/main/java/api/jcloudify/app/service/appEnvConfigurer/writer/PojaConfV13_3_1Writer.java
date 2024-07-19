package api.jcloudify.app.service.appEnvConfigurer.writer;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConfV1331;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
final class PojaConfV13_3_1Writer extends AbstractAppEnvConfigWriter {
  PojaConfV13_3_1Writer(@Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
    super(yamlObjectMapper);
  }

  @SneakyThrows
  @Override
  protected File writeToTempFile(PojaConf pojaConf) {
    var casted = (PojaConfV1331) pojaConf;
    File namedTempFile = createNamedTempFile("poja_v13_3_1.yml");
    this.yamlObjectMapper.writeValue(namedTempFile, casted);
    return namedTempFile;
  }

  @Override
  public File apply(OneOfPojaConf oneOfPojaConf) {
    return writeToTempFile(oneOfPojaConf.getPojaConfV1331());
  }
}
