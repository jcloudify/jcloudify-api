package api.jcloudify.app.endpoint.validator;

import api.jcloudify.app.endpoint.rest.model.CreateEnvironmentSsmParameters;
import api.jcloudify.app.model.exception.BadRequestException;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

@Component
public class CreateEnvironmentSsmParametersValidator
    implements Consumer<CreateEnvironmentSsmParameters> {
  @Override
  public void accept(CreateEnvironmentSsmParameters createEnvironmentSsmParameters) {
    if (createEnvironmentSsmParameters.getData() == null) {
      throw new BadRequestException("Data is mandatory.");
    }
  }
}
