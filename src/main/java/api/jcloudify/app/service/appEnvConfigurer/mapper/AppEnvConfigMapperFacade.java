package api.jcloudify.app.service.appEnvConfigurer.mapper;

import static api.jcloudify.app.endpoint.rest.model.PojaConf1.JSON_PROPERTY_GENERAL;
import static api.jcloudify.app.model.PojaVersion.POJA_1;
import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static api.jcloudify.app.model.pojaConf.conf1.PojaConf1.GeneralConf1.JSON_PROPERTY_CLI_VERSION;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.model.exception.ApiException;
import api.jcloudify.app.model.exception.NotImplementedException;
import api.jcloudify.app.service.appEnvConfigurer.NetworkingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public final class AppEnvConfigMapperFacade extends AbstractAppEnvConfigMapper {
  private final PojaConf1Mapper conf1Mapper;

  AppEnvConfigMapperFacade(
      @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper,
      NetworkingService networkingService,
      PojaConf1Mapper conf1Mapper) {
    super(yamlObjectMapper, networkingService);
    this.conf1Mapper = conf1Mapper;
  }

  @Override
  public OneOfPojaConf read(File file) {
    var pojaVersion = readToPojaConf(file);
    if (POJA_1.equals(pojaVersion)) {
      return conf1Mapper.read(file);
    }
    throw new NotImplementedException("not implemented yet");
  }

  private PojaVersion readToPojaConf(File file) {
    try {
      JsonNode jsonNode = yamlObjectMapper.readTree(file);
      String cliVersion =
          jsonNode.get(JSON_PROPERTY_GENERAL).get(JSON_PROPERTY_CLI_VERSION).asText();
      return PojaVersion.fromCliVersion(cliVersion)
          .orElseThrow(
              () ->
                  new ApiException(
                      SERVER_EXCEPTION,
                      "unable to convert cli_version of " + file.getName() + " to poja_version."));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public File write(OneOfPojaConf oneOfPojaConf) {
    var casted = (PojaConf) oneOfPojaConf.getActualInstance();
    if (POJA_1.toHumanReadableValue().equals(casted.getVersion())) {
      return conf1Mapper.write(oneOfPojaConf);
    }
    throw new NotImplementedException("not implemented yet");
  }

  @Override
  protected File writeToTempFile(PojaConf pojaConf) {
    if (POJA_1.toHumanReadableValue().equals(pojaConf.getVersion())) {
      return conf1Mapper.writeToTempFile(pojaConf);
    }
    throw new NotImplementedException("not implemented yet");
  }
}
