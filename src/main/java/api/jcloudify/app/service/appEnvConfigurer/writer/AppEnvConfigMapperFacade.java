package api.jcloudify.app.service.appEnvConfigurer.writer;

import static api.jcloudify.app.model.PojaVersion.POJA_V16_2_1;
import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static api.jcloudify.app.service.appEnvConfigurer.writer.mixins.PojaConfV16_2_1Mixin.CLI_VERSION_ATTRIBUTE;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.model.exception.ApiException;
import api.jcloudify.app.model.exception.NotImplementedException;
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
  private final PojaConfV16_2_1Mapper v16_2_1Mapper;

  AppEnvConfigMapperFacade(
      @Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper,
      PojaConfV16_2_1Mapper v16_2_1Mapper) {
    super(yamlObjectMapper);
    this.v16_2_1Mapper = v16_2_1Mapper;
  }

  @Override
  public OneOfPojaConf read(File file) {
    var pojaVersion = readToPojaConf(file);
    if (POJA_V16_2_1.equals(pojaVersion)) {
      return v16_2_1Mapper.read(file);
    }
    throw new NotImplementedException("not implemented yet");
  }

  private PojaVersion readToPojaConf(File file) {
    try {
      JsonNode jsonNode = yamlObjectMapper.readTree(file);
      String cliVersion = jsonNode.get(CLI_VERSION_ATTRIBUTE).asText();
      return PojaVersion.fromHumanReadableValue(cliVersion)
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
    if (POJA_V16_2_1.toHumanReadableValue().equals(casted.getVersion())) {
      return v16_2_1Mapper.write(oneOfPojaConf);
    }
    throw new NotImplementedException("not implemented yet");
  }

  @Override
  protected File writeToTempFile(PojaConf pojaConf) {
    if (POJA_V16_2_1.toHumanReadableValue().equals(pojaConf.getVersion())) {
      return v16_2_1Mapper.writeToTempFile(pojaConf);
    }
    throw new NotImplementedException("not implemented yet");
  }
}
