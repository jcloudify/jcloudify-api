package api.jcloudify.app.aws.cloudformation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.CreateStackResponse;
import software.amazon.awssdk.services.cloudformation.model.Parameter;
import software.amazon.awssdk.services.cloudformation.model.Tag;

import java.util.List;
import java.util.Map;

import static software.amazon.awssdk.services.cloudformation.model.Capability.CAPABILITY_NAMED_IAM;

@Component
@AllArgsConstructor
public class CloudformationComponent {
    private final CloudformationConf conf;

    public String deployStack(String stackName, String templateUrl, Map<String, String> parameters, Map<String, String> tags) {
        List<Parameter> stackParameters = parameters.entrySet().stream()
                .map(param -> Parameter.builder()
                        .parameterKey(param.getKey())
                        .parameterValue(param.getValue())
                        .build())
                .toList();

        List<Tag> stackTags = tags.entrySet().stream()
                .map(tag -> Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .toList();

        CreateStackRequest request = CreateStackRequest.builder()
                .stackName(stackName)
                .templateURL(templateUrl)
                .parameters(stackParameters)
                .tags(stackTags)
                .capabilities(CAPABILITY_NAMED_IAM)
                .build();

        return conf.getCloudformationClient().createStack(request).stackId();
    }
}
