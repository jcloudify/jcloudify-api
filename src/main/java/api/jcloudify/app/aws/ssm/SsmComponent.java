package api.jcloudify.app.aws.ssm;

import static software.amazon.awssdk.services.ssm.model.ParameterType.STRING;

import api.jcloudify.app.endpoint.rest.model.CreateSsmParameter;
import api.jcloudify.app.endpoint.rest.model.SsmParameter;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersRequest;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.Tag;

@Component
@AllArgsConstructor
public class SsmComponent {
  private final SsmClient ssmClient;

  public List<Parameter> updateSsmParameters(List<SsmParameter> ssmParameters) {
    return ssmParameters.stream().map(this::updateSsmParameter).toList();
  }

  public List<Parameter> createSsmParameters(
      List<CreateSsmParameter> ssmParameters, Map<String, String> tags) {
    return ssmParameters.stream().map(parameter -> createSsmParameter(parameter, tags)).toList();
  }

  private Parameter createSsmParameter(CreateSsmParameter parameter, Map<String, String> tags) {
    PutParameterRequest crupdateRequest =
        PutParameterRequest.builder()
            .name(parameter.getName())
            .value(parameter.getValue())
            .tags(setUpTags(tags))
            .type(STRING) // We only take in charge of STRING parameter types
            .build();
    ssmClient.putParameter(crupdateRequest);
    GetParameterRequest getRequest =
        GetParameterRequest.builder().name(parameter.getName()).build();
    return ssmClient.getParameter(getRequest).parameter();
  }

  private Parameter updateSsmParameter(SsmParameter parameter) {
    PutParameterRequest crupdateRequest =
        PutParameterRequest.builder()
            .name(parameter.getName())
            .value(parameter.getValue())
            .overwrite(true)
            .type(STRING) // We only take in charge of STRING parameter types
            .build();
    ssmClient.putParameter(crupdateRequest);
    GetParameterRequest getRequest =
        GetParameterRequest.builder().name(parameter.getName()).build();
    return ssmClient.getParameter(getRequest).parameter();
  }

  public List<Parameter> getSsmParametersByNames(List<String> parameterNames) {
    GetParametersRequest request = GetParametersRequest.builder().names(parameterNames).build();
    return ssmClient.getParameters(request).parameters();
  }

  private List<Tag> setUpTags(Map<String, String> tags) {
    return tags.entrySet().stream()
        .map(tag -> Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
        .toList();
  }
}
