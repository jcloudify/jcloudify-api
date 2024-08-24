package api.jcloudify.app.service;

import static java.lang.Math.min;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.aws.cloudformation.CloudformationTemplateConf;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.StackCrupdated;
import api.jcloudify.app.endpoint.rest.mapper.StackMapper;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.StackEvent;
import api.jcloudify.app.endpoint.rest.model.StackOutput;
import api.jcloudify.app.endpoint.rest.model.StackType;
import api.jcloudify.app.file.ExtendedBucketComponent;
import api.jcloudify.app.model.BoundedPageSize;
import api.jcloudify.app.model.Page;
import api.jcloudify.app.model.PageFromOne;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.StackRepository;
import api.jcloudify.app.repository.jpa.dao.StackDao;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.EnvDeploymentConf;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.Stack;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class StackService {
  public static final String STACK_EVENT_FILENAME = "log.json";
  public static final String STACK_OUTPUT_FILENAME = "output.json";
  private final CloudformationTemplateConf cloudformationTemplateConf;
  private final CloudformationComponent cloudformationComponent;
  private final EnvironmentService environmentService;
  private final ApplicationService applicationService;
  private final StackRepository repository;
  private final StackMapper mapper;
  private final StackDao dao;
  private final EventProducer<StackCrupdated> eventProducer;
  private final ExtendedBucketComponent bucketComponent;
  private final ObjectMapper om;
  private final EnvDeploymentConfService envDeploymentConfService;

  public Page<StackEvent> getStackEvents(
      String userId,
      String applicationId,
      String environmentId,
      String stackId,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    String stackEventsBucketKey =
        getStackEventsBucketKey(
            userId, applicationId, environmentId, stackId, STACK_EVENT_FILENAME);
    return getPagedStackData(
        stackId, stackEventsBucketKey, pageFromOne, boundedPageSize, StackEvent.class);
  }

  public Page<StackOutput> getStackOutputs(
      String userId,
      String applicationId,
      String environmentId,
      String stackId,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    String stackOutputsBucketKey =
        getStackOutputsBucketKey(
            userId, applicationId, environmentId, stackId, STACK_OUTPUT_FILENAME);
    return getPagedStackData(
        stackId, stackOutputsBucketKey, pageFromOne, boundedPageSize, StackOutput.class);
  }

  private <T> Page<T> getPagedStackData(
      String stackId,
      String bucketKey,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize,
      Class<T> clazz) {
    try {
      List<T> stackData = fromStackDataFileToList(bucketComponent, om, stackId, bucketKey, clazz);
      if (!stackData.isEmpty()) {
        int firstIndex = (pageFromOne.getValue() - 1) * boundedPageSize.getValue();
        int lastIndex = min(firstIndex + boundedPageSize.getValue(), stackData.size());
        var data = stackData.subList(firstIndex, lastIndex);
        return new Page<>(pageFromOne, boundedPageSize, data);
      }
      return new Page<>(pageFromOne, boundedPageSize, stackData);
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }

  public List<api.jcloudify.app.endpoint.rest.model.Stack> process(
      List<InitiateDeployment> deployments,
      String userId,
      String applicationId,
      String environmentId) {
    return deployments.stream()
        .map(stack -> this.deployStack(stack, userId, applicationId, environmentId))
        .toList();
  }

  public Page<api.jcloudify.app.endpoint.rest.model.Stack> findAllBy(
      String userId,
      String applicationId,
      String environmentId,
      PageFromOne pageFromOne,
      BoundedPageSize boundedPageSize) {
    var data =
        dao
            .findAllByCriteria(
                userId,
                applicationId,
                environmentId,
                PageRequest.of(pageFromOne.getValue() - 1, boundedPageSize.getValue()))
            .stream()
            .map(this::toRestWithApplicationAndEnvironment)
            .toList();
    return new Page<>(pageFromOne, boundedPageSize, data);
  }

  public api.jcloudify.app.endpoint.rest.model.Stack getById(
      String userId, String applicationId, String environmentId, String stackId) {
    assert userId != null;
    return toRestWithApplicationAndEnvironment(
        repository
            .findByApplicationIdAndEnvironmentIdAndId(applicationId, environmentId, stackId)
            .orElseThrow(() -> new NotFoundException("Stack id=" + stackId + " not found")));
  }

  private api.jcloudify.app.endpoint.rest.model.Stack toRestWithApplicationAndEnvironment(
      Stack domain) {
    Application application = applicationService.getById(domain.getApplicationId());
    Environment environment = environmentService.getById(domain.getEnvironmentId());
    return mapper.toRest(domain, application, environment);
  }

  public Stack save(Stack toSave) {
    return repository.save(toSave);
  }

  private api.jcloudify.app.endpoint.rest.model.Stack deployStack(
      InitiateDeployment toDeploy, String userId, String applicationId, String environmentId) {
    Application application = applicationService.getById(applicationId);
    Environment environment = environmentService.getById(environmentId);
    String environmentType = environment.getFormattedEnvironmentType();
    String applicationName = application.getFormattedName();
    EnvDeploymentConf envDeploymentConf = envDeploymentConfService.getLatestByEnvId(environmentId);
    Map<String, String> parameters = getParametersFrom(environmentType);
    Map<String, String> tags = setUpTags(applicationName, environmentType);
    Optional<Stack> stack =
        dao.findByCriteria(applicationId, environmentId, toDeploy.getStackType());
    if (stack.isPresent()) {
      Stack toUpdate = stack.get();
      String cfStackId =
          updateStack(
              userId,
              applicationId,
              environmentId,
              toDeploy,
              parameters,
              envDeploymentConf,
              toUpdate.getName(),
              tags);
      if (cfStackId != null) {
        Stack saved =
            save(
                Stack.builder()
                    .id(toUpdate.getId())
                    .name(toUpdate.getName())
                    .cfStackId(cfStackId)
                    .applicationId(applicationId)
                    .environmentId(environmentId)
                    .type(toUpdate.getType())
                    .creationDatetime(toUpdate.getCreationDatetime())
                    .build());
        eventProducer.accept(List.of(StackCrupdated.builder().userId(userId).stack(saved).build()));
        return mapper.toRest(saved, application, environment);
      }
      return mapper.toRest(toUpdate, application, environment);
    } else {
      String stackName =
          String.format(
              "%s-%s-%s",
              environmentType,
              String.valueOf(toDeploy.getStackType()).toLowerCase().replace("_", "-"),
              applicationName);
      String cfStackId =
          createStack(
              userId,
              applicationId,
              environmentId,
              toDeploy,
              parameters,
              envDeploymentConf,
              stackName,
              tags);
      Stack saved =
          save(
              Stack.builder()
                  .name(stackName)
                  .cfStackId(cfStackId)
                  .applicationId(applicationId)
                  .environmentId(environmentId)
                  .type(toDeploy.getStackType())
                  .build());
      eventProducer.accept(List.of(StackCrupdated.builder().userId(userId).stack(saved).build()));
      return mapper.toRest(saved, application, environment);
    }
  }

  public static String getStackEventsBucketKey(
      String userId, String appId, String envId, String stackId, String filename) {
    return String.format(
        "users/%s/apps/%s/envs/%s/stacks/%s/events/%s", userId, appId, envId, stackId, filename);
  }

  public static String getStackOutputsBucketKey(
      String userId, String appId, String envId, String stackId, String filename) {
    return String.format(
        "users/%s/apps/%s/envs/%s/stacks/%s/outputs/%s", userId, appId, envId, stackId, filename);
  }

  private static <T> List<T> fromStackDataFileToList(
      ExtendedBucketComponent bucketComponent,
      ObjectMapper om,
      String stackId,
      String bucketKey,
      Class<T> clazz)
      throws IOException {
    if (bucketComponent.doesExist(bucketKey)) {
      File stackDataFile = bucketComponent.download(bucketKey);
      return om.readValue(
          stackDataFile, om.getTypeFactory().constructCollectionType(List.class, clazz));
    }
    return List.of();
  }

  private static Map<String, String> getParametersFrom(String environmentType) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("Env", environmentType);
    return parameters;
  }

  private String getStackTemplateUrlFrom(
      String userId,
      String appId,
      String envId,
      StackType type,
      EnvDeploymentConf envDeploymentConf) {
    Map<StackType, Supplier<String>> stackFileKeyMap =
        Map.of(
            StackType.EVENT, envDeploymentConf::getEventStackFileKey,
            StackType.STORAGE_BUCKET, envDeploymentConf::getStorageBucketStackFileKey,
            StackType.COMPUTE_PERMISSION, envDeploymentConf::getComputePermissionStackFileKey,
            StackType.STORAGE_DATABASE_SQLITE,
                envDeploymentConf::getStorageDatabaseSqliteStackFileKey);
    String filename = stackFileKeyMap.getOrDefault(type, () -> null).get();
    return cloudformationTemplateConf
        .getCloudformationTemplateUrl(userId, appId, envId, filename)
        .toString();
  }

  private String createStack(
      String userId,
      String appId,
      String envId,
      InitiateDeployment toDeploy,
      Map<String, String> parameters,
      EnvDeploymentConf envDeploymentConf,
      String stackName,
      Map<String, String> tags) {
    return cloudformationComponent.createStack(
        stackName,
        getStackTemplateUrlFrom(userId, appId, envId, toDeploy.getStackType(), envDeploymentConf),
        parameters,
        tags);
  }

  private String updateStack(
      String userId,
      String appId,
      String envId,
      InitiateDeployment toDeploy,
      Map<String, String> parameters,
      EnvDeploymentConf envDeploymentConf,
      String stackName,
      Map<String, String> tags) {
    return cloudformationComponent.updateStack(
        stackName,
        getStackTemplateUrlFrom(userId, appId, envId, toDeploy.getStackType(), envDeploymentConf),
        parameters,
        tags);
  }

  public static Map<String, String> setUpTags(String applicationName, String applicationEnv) {
    Map<String, String> tags = new HashMap<>();
    tags.put("app", applicationName);
    tags.put("env", applicationEnv);
    tags.put("user:poja", applicationName);
    return tags;
  }

  public String getCloudformationStackId(String stackName) {
    return cloudformationComponent.getStackIdByName(stackName);
  }
}
