package api.jcloudify.app.endpoint.validator;

import static api.jcloudify.app.file.ExtendedBucketComponent.TEMP_BUCKET_PATH;

import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.model.exception.BadRequestException;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class BuiltEnvInfoValidator implements Consumer<BuiltEnvInfo> {
  @Override
  public void accept(BuiltEnvInfo builtEnvInfo) {
    if (builtEnvInfo == null) {
      throw new BadRequestException("builtEnvInfo is mandatory.");
    }
    if (builtEnvInfo.getFormattedBucketKey() == null) {
      throw new BadRequestException("formattedBucketKey is mandatory.");
    }
    if (!builtEnvInfo.getFormattedBucketKey().startsWith(TEMP_BUCKET_PATH)) {
      throw new BadRequestException(
          "cannot use files not from temporary bucket key. Use " + TEMP_BUCKET_PATH + " instead.");
    }
  }
}
