package api.jcloudify.app.service;

import api.jcloudify.app.aws.ssm.SsmComponent;
import api.jcloudify.app.endpoint.rest.mapper.SsmParameterMapper;
import api.jcloudify.app.endpoint.rest.model.SsmParameter;
import api.jcloudify.app.repository.jpa.SsmParameterRepository;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.List;
import java.util.Map;

import static api.jcloudify.app.service.StackService.setUpTags;

@Service
@AllArgsConstructor
public class SsmParameterService {
    private final SsmParameterRepository repository;
    private final SsmComponent ssmComponent;
    private final ApplicationService applicationService;
    private final EnvironmentService environmentService;
    private final SsmParameterMapper mapper;

    public List<SsmParameter> crupdateParameters(String appId, String envId, List<SsmParameter> parameters) {
        Application application = applicationService.getById(appId);
        Environment environment = environmentService.getById(envId);
        Map<String, String> tags = setUpTags(application.getName(), environment.getEnvironmentType().toString().toLowerCase());
        List<Parameter> createdParameters = ssmComponent.crupdateSsmParameters(parameters, tags);
        List<api.jcloudify.app.repository.model.SsmParameter> saved = saveAll(mapper.toDomain(parameters, envId));
        return mapper.toRest(saved, createdParameters);
    }

    private List<api.jcloudify.app.repository.model.SsmParameter> saveAll(List<api.jcloudify.app.repository.model.SsmParameter> parameters) {
        return repository.saveAll(parameters);
    }
}
