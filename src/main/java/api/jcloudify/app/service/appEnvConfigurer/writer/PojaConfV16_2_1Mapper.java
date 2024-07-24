package api.jcloudify.app.service.appEnvConfigurer.writer;

import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConfV1621;
import api.jcloudify.app.model.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
final class PojaConfV16_2_1Mapper extends AbstractAppEnvConfigMapper {
  PojaConfV16_2_1Mapper(@Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper) {
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

  public OneOfPojaConf read(File file) {
    PojaConfV1621 pojaConf;
    try {
      pojaConf = yamlObjectMapper.readValue(file, PojaConfV1621.class);
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
    return new OneOfPojaConf(pojaConf);
  }

  @Override
  public File write(OneOfPojaConf oneOfPojaConf) {
    return writeToTempFile(oneOfPojaConf.getPojaConfV1621());
  }
}
