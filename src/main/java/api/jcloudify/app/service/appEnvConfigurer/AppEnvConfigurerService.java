package api.jcloudify.app.service.appEnvConfigurer;

import static api.jcloudify.app.file.ExtendedBucketComponent.getBucketKey;
import static api.jcloudify.app.file.FileType.POJA_CONF;
import static api.jcloudify.app.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.endpoint.rest.model.OneOfPojaConf;
import api.jcloudify.app.endpoint.rest.model.PojaConf;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.model.PojaVersion;
import api.jcloudify.app.model.exception.ApiException;
import api.jcloudify.app.service.appEnvConfigurer.writer.PojaConfFileMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppEnvConfigurerService {
  private final PojaConfFileMapper mapper;
  private final ExtendedBucketComponent bucketComponent;
  private final EventProducer<PojaConfUploaded> pojaConfUploadedEventProducer;

  public OneOfPojaConf configureEnvironment(
      String userId, String appId, String environmentId, OneOfPojaConf pojaConf) {
    var validatedFile = mapper.write(pojaConf);
    bucketComponent.upload(
        validatedFile,
        getBucketKey(userId, appId, environmentId, POJA_CONF, validatedFile.getName()));
    PojaConf baseClass = (PojaConf) pojaConf.getActualInstance();
    PojaVersion pojaVersion =
        PojaVersion.fromHumanReadableValue(baseClass.getVersion())
            .orElseThrow(() -> new ApiException(SERVER_EXCEPTION, "unable to get poja version"));
    PojaConfUploaded relatedEvent =
        new PojaConfUploaded(pojaVersion, environmentId, userId, validatedFile.getName(), appId);
    pojaConfUploadedEventProducer.accept(List.of(relatedEvent));
    return pojaConf;
  }

  public OneOfPojaConf readConfig(
      String userId, String appId, String environmentId, String filename) {
    var file =
        bucketComponent.download(getBucketKey(userId, appId, environmentId, POJA_CONF, filename));
    return mapper.read(file);
  }
}
