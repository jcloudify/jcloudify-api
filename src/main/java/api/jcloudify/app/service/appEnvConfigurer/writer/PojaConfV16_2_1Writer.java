package api.jcloudify.app.service.appEnvConfigurer.writer;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConfV1621;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
final class PojaConfV16_2_1Writer extends AbstractAppEnvConfigWriter {
  PojaConfV16_2_1Writer(@Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
    super(yamlObjectMapper);
  }

  @SneakyThrows
  @Override
  protected File writeToTempFile(PojaConf pojaConf) {
    var casted = (PojaConfV1621) pojaConf;
    File namedTempFile = createNamedTempFile("poja_v16_2_1.yml");
    this.yamlObjectMapper.writeValue(namedTempFile, casted);
    return namedTempFile;
  }

  @Override
  public File apply(OneOfPojaConf oneOfPojaConf) {
    return writeToTempFile(oneOfPojaConf.getPojaConfV1621());
  }
}
