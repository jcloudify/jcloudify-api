package api.jcloudify.app.endpoint.validator;

import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironmentSsmParameters;
import api.jcloudify.app.model.exception.BadRequestException;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class CrupdateEnvironmentSsmParametersValidator
    implements Consumer<CrupdateEnvironmentSsmParameters> {
  @Override
  public void accept(CrupdateEnvironmentSsmParameters crupdateEnvironmentSsmParameters) {
    if (crupdateEnvironmentSsmParameters.getData() == null) {
      throw new BadRequestException("Data is mandatory.");
    }
  }
}
