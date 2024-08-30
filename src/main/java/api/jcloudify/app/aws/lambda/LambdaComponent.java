package api.jcloudify.app.aws.lambda;

import api.jcloudify.app.aws.lambda.model.LambdaConfiguration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Component
@AllArgsConstructor
public class LambdaComponent {
  private final LambdaClient client;

  public LambdaConfiguration getLambdaConfiguration(String functionName) {
    var awsModel = client.getFunctionConfiguration(req -> req.functionName(functionName));
    var loggingConfig = awsModel.loggingConfig();
    return new LambdaConfiguration(
        awsModel.functionName(), new LambdaConfiguration.LoggingConfig(loggingConfig.logGroup()));
  }
}
