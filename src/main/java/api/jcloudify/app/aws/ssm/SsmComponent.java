package api.jcloudify.app.aws.ssm;

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

  public List<Parameter> crupdateSsmParameters(
      List<SsmParameter> ssmParameters, Map<String, String> tags) {
    return ssmParameters.stream().map(parameter -> crupdateSsmParameter(parameter, tags)).toList();
  }

  private Parameter crupdateSsmParameter(SsmParameter parameter, Map<String, String> tags) {
    PutParameterRequest crupdateRequest =
        PutParameterRequest.builder()
            .name(parameter.getName())
            .value(parameter.getValue())
            .tags(setUpTags(tags))
            .overwrite(true) // directly set to true to allow updating parameter
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
