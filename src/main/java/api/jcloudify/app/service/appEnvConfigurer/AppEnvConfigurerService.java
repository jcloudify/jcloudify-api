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
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.EnvironmentRepository;
import api.jcloudify.app.service.appEnvConfigurer.mapper.PojaConfFileMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AppEnvConfigurerService {
  private final PojaConfFileMapper mapper;
  private final ExtendedBucketComponent bucketComponent;
  private final EventProducer<PojaConfUploaded> pojaConfUploadedEventProducer;
  private final EnvironmentRepository environmentRepository;

  @Transactional
  public OneOfPojaConf configureEnvironment(
      String userId, String appId, String environmentId, OneOfPojaConf pojaConf) {
    var validatedFile = mapper.write(pojaConf);
    String nonFormattedFilename = validatedFile.getName();
    String formattedFilename =
        getBucketKey(userId, appId, environmentId, POJA_CONF, nonFormattedFilename);
    bucketComponent.upload(validatedFile, formattedFilename);
    environmentRepository.updateEnvironmentConfigFileKey(environmentId, nonFormattedFilename);
    PojaConf baseClass = (PojaConf) pojaConf.getActualInstance();
    PojaVersion pojaVersion =
        PojaVersion.fromHumanReadableValue(baseClass.getVersion())
            .orElseThrow(() -> new ApiException(SERVER_EXCEPTION, "unable to get poja version"));
    PojaConfUploaded relatedEvent =
        new PojaConfUploaded(pojaVersion, environmentId, userId, nonFormattedFilename, appId);
    pojaConfUploadedEventProducer.accept(List.of(relatedEvent));
    return pojaConf;
  }

  public OneOfPojaConf readConfig(
      String userId, String appId, String environmentId, String filename) {
    String bucketKey = getBucketKey(userId, appId, environmentId, POJA_CONF, filename);
    if (!bucketComponent.doesExist(bucketKey)) {
      throw new NotFoundException(
          "config not found in S3 for user.Id = "
              + userId
              + " app.Id = "
              + appId
              + " environment.Id = "
              + environmentId);
    }
    var file = bucketComponent.download(bucketKey);
    return mapper.read(file);
  }

  public api.jcloudify.app.model.pojaConf.conf1.PojaConf readConfigAsDomain(
      String userId, String appId, String environmentId, String filename) {
    String bucketKey = getBucketKey(userId, appId, environmentId, POJA_CONF, filename);
    if (!bucketComponent.doesExist(bucketKey)) {
      throw new NotFoundException(
          "config not found in S3 for user.Id = "
              + userId
              + " app.Id = "
              + appId
              + " environment.Id = "
              + environmentId);
    }
    var file = bucketComponent.download(bucketKey);
    return mapper.readAsDomain(file);
  }
}
