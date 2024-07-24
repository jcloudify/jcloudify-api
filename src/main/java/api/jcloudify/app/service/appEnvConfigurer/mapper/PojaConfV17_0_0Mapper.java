package api.jcloudify.app.service.appEnvConfigurer.mapper;

import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConfV1700;
import api.jcloudify.app.model.exception.ApiException;
import api.jcloudify.app.model.pojaConf.v17_0_0.PojaConfV17_0_0;
import api.jcloudify.app.service.appEnvConfigurer.NetworkingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
final class PojaConfV17_0_0Mapper extends AbstractAppEnvConfigMapper {
  PojaConfV17_0_0Mapper(
      @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper,
      NetworkingService networkingService) {
    super(yamlObjectMapper, networkingService);
  }

  @SneakyThrows
  @Override
  protected File writeToTempFile(PojaConf pojaConf) {
    var casted = (PojaConfV1700) pojaConf;
    var domainPojaConf =
        new PojaConfV17_0_0(casted, networkingService.getNetworkingConfig(), null, null, null);
    File namedTempFile = createNamedTempFile("poja_v17_0_0.yml");
    this.yamlObjectMapper.writeValue(namedTempFile, domainPojaConf);
    return namedTempFile;
  }

  public OneOfPojaConf read(File file) {
    PojaConfV1700 pojaConf;
    try {
      pojaConf = yamlObjectMapper.readValue(file, PojaConfV1700.class);
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
    return new OneOfPojaConf(pojaConf);
  }

  @Override
  public File write(OneOfPojaConf oneOfPojaConf) {
    return writeToTempFile(oneOfPojaConf.getPojaConfV1700());
  }
}
