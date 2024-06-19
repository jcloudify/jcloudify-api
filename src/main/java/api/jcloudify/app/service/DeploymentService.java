package api.jcloudify.app.service;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.aws.cloudformation.CloudformationConf;
import api.jcloudify.app.endpoint.rest.model.DeploymentInitiated;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.model.exception.BadRequestException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class DeploymentService {
    private final CloudformationConf cloudformationConf;
    private final CloudformationComponent cloudformationComponent;

    public List<DeploymentInitiated> process(List<InitiateDeployment> deployments) {
        return deployments.stream()
                .map(this::deployStack)
                .toList();
    }

    private DeploymentInitiated deployStack(InitiateDeployment toDeploy) {
        String applicationName = toDeploy.getApplicationName();
        String applicationEnv = String.valueOf(toDeploy.getEnvironmentType()).toLowerCase();
        Map<String, String> tags = new HashMap<>();
        tags.put("app", applicationName);
        tags.put("env", applicationEnv);
        tags.put("user:poja", applicationName);
        String stackName = String
                .format(
                        "%s-%s-%s", applicationEnv, String.valueOf(toDeploy.getStack()).toLowerCase(), applicationName);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("Env", applicationEnv);
        parameters.put("AppName", applicationName);
        String stackId;
        switch (toDeploy.getStack()) {
            case EVENT -> {
                parameters.put("Prefix", "1");
                stackId = cloudformationComponent.deployStack(stackName, cloudformationConf.EVENT_STACK_URL, parameters, tags);
            }
            case COMPUTE_PERMISSION -> {
                stackId = cloudformationComponent.deployStack(stackName, cloudformationConf.COMPUTE_PERMISSION_STACK_URL, parameters, tags);
            }
            case STORAGE_BUCKET -> {
                stackId = cloudformationComponent.deployStack(stackName, cloudformationConf.STORAGE_BUCKET_STACK_URL, parameters, tags);
            }
            case STORAGE_DATABASE -> {
                stackId = cloudformationComponent.deployStack(stackName, cloudformationConf.STORAGE_DATABASE_STACK_URL, parameters, tags);
            }
            case null -> throw new BadRequestException("Stack type to deploy must be defined");
        }
        return new DeploymentInitiated()
                .environmentType(toDeploy.getEnvironmentType())
                .applicationName(applicationName)
                .stack(toDeploy.getStack())
                .stackId(stackId);
    }
}
