package api.jcloudify.app.aws.cloudformation;

import static software.amazon.awssdk.services.cloudformation.model.Capability.CAPABILITY_NAMED_IAM;

import api.jcloudify.app.model.exception.BadRequestException;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.model.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CloudFormationException;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.DeleteStackRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackEventsRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourcesRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStackResourcesResponse;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Output;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackEvent;
import software.amazon.awssdk.services.cloudformation.model.StackResource;
import software.amazon.awssdk.services.cloudformation.model.Tag;
import software.amazon.awssdk.services.cloudformation.model.UpdateStackRequest;

@Component
@AllArgsConstructor
public class CloudformationComponent {
  private final CloudFormationClient cloudFormationClient;

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
      return cloudFormationClient.createStack(request).stackId();
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
      return cloudFormationClient.updateStack(request).stackId();
    } catch (CloudFormationException e) {
      if (e.getMessage().contains("No updates are to be performed")) {
        return null;
      }
      throw new InternalServerErrorException(
          String.format(
              "An error occurred during stack(%s) update: %s", stackName, e.getMessage()));
    }
  }

  private Stack getStackByName(String stackName) {
    DescribeStacksRequest request = DescribeStacksRequest.builder().stackName(stackName).build();

    try {
      DescribeStacksResponse response = cloudFormationClient.describeStacks(request);
      if (!response.hasStacks()) {
        throw new NotFoundException("Stack(" + stackName + ") not found");
      }
      return response.stacks().getFirst();
    } catch (AwsServiceException | SdkClientException e) {
      if (e.getMessage().contains("Stack with id " + stackName + " does not exist")) {
        throw new NotFoundException("Stack(" + stackName + ") does not exist");
      }
      throw new InternalServerErrorException(e);
    }
  }

  public String getStackIdByName(String stackName) {
    return this.getStackByName(stackName).stackId();
  }

  public List<StackEvent> getStackEvents(String stackName) {
    DescribeStackEventsRequest request =
        DescribeStackEventsRequest.builder().stackName(stackName).build();
    try {
      return cloudFormationClient.describeStackEvents(request).stackEvents();
    } catch (CloudFormationException e) {
      throw new InternalServerErrorException(
          String.format(
              "An error occurred when retrieving stack(%s) events: %s", stackName, e.getMessage()));
    }
  }

  public List<Output> getStackOutputs(String stackName) {
    return this.getStackByName(stackName).outputs();
  }

  public void deleteStack(String stackName) {
    this.getStackByName(stackName);
    DeleteStackRequest request = DeleteStackRequest.builder().stackName(stackName).build();
    try {
      cloudFormationClient.deleteStack(request);
    } catch (AwsServiceException | SdkClientException e) {
      if (e.getMessage().contains("Stack with id " + stackName + " does not exist")) {
        throw new NotFoundException("Stack(" + stackName + ") does not exist");
      }
      throw new InternalServerErrorException(e);
    }
  }

  public List<StackResource> getStackResources(String stackName) {
    DescribeStackResourcesRequest request =
        DescribeStackResourcesRequest.builder().stackName(stackName).build();
    try {
      DescribeStackResourcesResponse response =
          cloudFormationClient.describeStackResources(request);
      if (!response.hasStackResources()) {
        throw new NotFoundException("Stack(" + stackName + ") does not exist");
      }
      return response.stackResources();
    } catch (AwsServiceException | SdkClientException e) {
      throw new RuntimeException(e);
    }
  }
}
