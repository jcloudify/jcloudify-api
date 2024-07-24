package api.jcloudify.app.service.appEnvConfigurer;

import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.FileType.POJA_CONF;

import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.service.appEnvConfigurer.writer.PojaConfFileMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppEnvConfigurerService {
  private final PojaConfFileMapper mapper;
  private final ExtendedBucketComponent bucketComponent;

  public OneOfPojaConf configureEnvironment(
      String userId, String appId, String environmentId, OneOfPojaConf pojaConf) {
    var validatedFile = mapper.write(pojaConf);
    bucketComponent.upload(
        validatedFile,
        getBucketKey(userId, appId, environmentId, POJA_CONF, validatedFile.getName()));
    return pojaConf;
  }

  public OneOfPojaConf readConfig(
      String userId, String appId, String environmentId, String filename) {
    var file =
        bucketComponent.download(getBucketKey(userId, appId, environmentId, POJA_CONF, filename));
    return mapper.read(file);
  }
}
