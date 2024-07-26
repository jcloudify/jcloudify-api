package api.jcloudify.app.service.appEnvConfigurer.mapper;

import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.model.exception.ApiException;
import api.jcloudify.app.model.pojaConf.conf1.PojaConf1;
import api.jcloudify.app.service.appEnvConfigurer.NetworkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
final class PojaConf1Mapper extends AbstractAppEnvConfigMapper {
  PojaConf1Mapper(
      @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper,
      NetworkingService networkingService) {
    super(yamlObjectMapper, networkingService);
  }

  @SneakyThrows
  @Override
  protected File writeToTempFile(PojaConf pojaConf) {
    var casted = (api.jcloudify.app.endpoint.rest.model.PojaConf1) pojaConf;
    var domainPojaConf =
        new PojaConf1(casted, networkingService.getNetworkingConfig(), null, null, null);
    File namedTempFile = createNamedTempFile("poja_1.yml");
    this.yamlObjectMapper.writeValue(namedTempFile, domainPojaConf);
    return namedTempFile;
  }

  public OneOfPojaConf read(File file) {
    api.jcloudify.app.endpoint.rest.model.PojaConf1 pojaConf;
    try {
      pojaConf =
          yamlObjectMapper.readValue(file, api.jcloudify.app.endpoint.rest.model.PojaConf1.class);
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
    return new OneOfPojaConf(pojaConf);
  }

  @Override
  public File write(OneOfPojaConf oneOfPojaConf) {
    return writeToTempFile(oneOfPojaConf.getPojaConf1());
  }
}
