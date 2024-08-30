package api.jcloudify.app.aws.lambda.model;

public record LambdaConfiguration(String functionName, LoggingConfig loggingConfig) {
  public record LoggingConfig(String logGroupName) {}
}
