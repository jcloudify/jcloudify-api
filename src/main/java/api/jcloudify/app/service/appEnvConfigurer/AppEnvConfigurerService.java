package api.jcloudify.app.service.appEnvConfigurer;

import static api.jcloudify.app.file.FileType.POJA_CONF;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.service.appEnvConfigurer.writer.PojaConfFileWriter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppEnvConfigurerService {
  private final PojaConfFileWriter writer;
  private final ExtendedBucketComponent bucketComponent;

  public OneOfPojaConf configureEnvironment(
      String userId, String appId, String environmentId, OneOfPojaConf pojaConf) {
    var validatedFile = writer.apply(pojaConf);
    bucketComponent.upload(
        userId, appId, environmentId, POJA_CONF, validatedFile.getName(), validatedFile);
    return pojaConf;
  }
}
