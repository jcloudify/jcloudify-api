package api.jcloudify.app.service.event;

import static api.jcloudify.app.file.FileType.POJA_CONF;

import api.jcloudify.app.endpoint.event.model.PojaConfUploaded;
import api.jcloudify.app.file.ExtendedBucketComponent;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PojaConfUploadedService implements Consumer<PojaConfUploaded> {
  private final ExtendedBucketComponent bucketComponent;

  @Override
  public void accept(PojaConfUploaded pojaConfUploaded) {
    var pojaConf = bucketComponent.download(
        ExtendedBucketComponent.getBucketKey(
            pojaConfUploaded.getUserId(),
            pojaConfUploaded.getAppId(),
            pojaConfUploaded.getEnvironmentId(),
            POJA_CONF,
            pojaConfUploaded.getFilename()));
  }
}
