package api.jcloudify.app.aws.cloudformation;

import static software.amazon.awssdk.services.cloudformation.model.Capability.CAPABILITY_NAMED_IAM;

import api.jcloudify.app.model.exception.BadRequestException;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudformation.model.CloudFormationException;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.cloudformation.model.Tag;
import software.amazon.awssdk.services.cloudformation.model.UpdateStackRequest;

@Component
@AllArgsConstructor
public class CloudformationComponent {
  private final CloudformationConf conf;

  private static List<Tag> setUpTags(Map<String, String> tags) {
    return tags.entrySet().stream()
        .map(tag -> Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
        .toList();
  }

  private static List<Parameter> setUpParameters(Map<String, String> parameters) {
    return parameters.entrySet().stream()
        .map(
            param ->
                Parameter.builder()
                    .parameterKey(param.getKey())
                    .parameterValue(param.getValue())
                    .build())
        .toList();
  }

  public String createStack(
      String stackName,
      String templateUrl,
      Map<String, String> parameters,
      Map<String, String> tags) {
    List<Parameter> stackParameters = setUpParameters(parameters);
    List<Tag> stackTags = setUpTags(tags);

    CreateStackRequest request =
        CreateStackRequest.builder()
            .stackName(stackName)
            .templateURL(templateUrl)
            .parameters(stackParameters)
            .tags(stackTags)
            .capabilities(CAPABILITY_NAMED_IAM)
            .build();
    try {
      return conf.getCloudformationClient().createStack(request).stackId();
    } catch (CloudFormationException e) {
      throw new BadRequestException(
          String.format(
              "An error occurred during stack(%s) creation: %s", stackName, e.getMessage()));
    }
  }

  public String updateStack(
      String stackName,
      String templateUrl,
      Map<String, String> parameters,
      Map<String, String> tags) {
    List<Parameter> stackParameters = setUpParameters(parameters);
    List<Tag> stackTags = setUpTags(tags);

    UpdateStackRequest request =
        UpdateStackRequest.builder()
            .parameters(stackParameters)
            .templateURL(templateUrl)
            .stackName(stackName)
            .tags(stackTags)
            .capabilities(CAPABILITY_NAMED_IAM)
            .build();

    try {
      return conf.getCloudformationClient().updateStack(request).stackId();
    } catch (CloudFormationException e) {
      throw new BadRequestException(
          String.format(
              "An error occurred during stack(%s) update: %s", stackName, e.getMessage()));
    }
  }
}