package api.jcloudify.app.service;

import static java.lang.Math.min;
import static java.util.Objects.requireNonNull;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.aws.cloudformation.CloudformationTemplateConf;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.StackCrupdated;
import api.jcloudify.app.endpoint.rest.mapper.StackMapper;
import api.jcloudify.app.endpoint.rest.model.InitiateDeployment;
import api.jcloudify.app.endpoint.rest.model.StackEvent;
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
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.Stack;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class StackService {
  public static final String STACK_EVENT_FILENAME = "log.json";
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
    try {
      List<StackEvent> stackEvents =
          fromStackEventFileToList(bucketComponent, om, stackId, stackEventsBucketKey);
      int firstIndex = (pageFromOne.getValue() - 1) * boundedPageSize.getValue();
      int lastIndex = min(firstIndex + boundedPageSize.getValue() - 1, stackEvents.size() - 1);
      var data = stackEvents.subList(firstIndex, lastIndex);
      return new Page<>(pageFromOne, boundedPageSize, data);
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

  private Stack save(Stack toSave) {
    return repository.save(toSave);
  }

  private api.jcloudify.app.endpoint.rest.model.Stack deployStack(
      InitiateDeployment toDeploy, String userId, String applicationId, String environmentId) {
    Application application = applicationService.getById(applicationId);
    Environment environment = environmentService.getById(environmentId);
    String environmentType = environment.getFormattedEnvironmentType();
    String applicationName = application.getFormattedName();
    Map<String, String> parameters = getParametersFrom(environmentType, applicationName);

    Optional<Stack> stack =
        dao.findByCriteria(applicationId, environmentId, toDeploy.getStackType());
    if (stack.isPresent()) {
      Stack toUpdate = stack.get();
      Map<String, String> tags = setUpTags(toUpdate.getName(), environmentType);
      String cfStackId = updateStack(toDeploy, parameters, toUpdate.getName(), tags);
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
    } else {
      String stackName =
          String.format(
              "%s-%s-%s",
              environmentType,
              String.valueOf(toDeploy.getStackType()).toLowerCase().replace("_", "-"),
              applicationName);
      Map<String, String> tags = setUpTags(stackName, environmentType);
      String cfStackId = createStack(toDeploy, parameters, stackName, tags);
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

  private static List<StackEvent> fromStackEventFileToList(
      ExtendedBucketComponent bucketComponent,
      ObjectMapper om,
      String stackId,
      String stackEventsBucketKey)
      throws IOException {
    if (bucketComponent.doesExist(stackEventsBucketKey)) {
      File stackEventsFile = bucketComponent.download(stackEventsBucketKey);
      return om.readValue(stackEventsFile, new TypeReference<>() {});
    }
    throw new NotFoundException("No events found for stack id=" + stackId); // Unreachable statement
  }

  private static Map<String, String> getParametersFrom(
      String environmentType, String applicationName) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("Env", environmentType);
    parameters.put("AppName", applicationName);
    return parameters;
  }

  private String getStackTemplateUrlFrom(StackType stackType) {
    return (switch (stackType) {
          case EVENT -> cloudformationTemplateConf.getEventStackTemplateUrl();
          case COMPUTE_PERMISSION -> cloudformationTemplateConf
              .getComputePermissionStackTemplateUrl();
          case STORAGE_BUCKET -> cloudformationTemplateConf.getStorageBucketStackTemplateUrl();
          case STORAGE_DATABASE_SQLITE -> cloudformationTemplateConf
              .getStorageDatabaseSQliteStackTemplateUrl();
        })
        .toString();
  }

  private String createStack(
      InitiateDeployment toDeploy,
      Map<String, String> parameters,
      String stackName,
      Map<String, String> tags) {
    return cloudformationComponent.createStack(
        stackName,
        getStackTemplateUrlFrom(requireNonNull(toDeploy.getStackType())),
        parameters,
        tags);
  }

  private String updateStack(
      InitiateDeployment toDeploy,
      Map<String, String> parameters,
      String stackName,
      Map<String, String> tags) {
    return cloudformationComponent.updateStack(
        stackName,
        getStackTemplateUrlFrom(requireNonNull(toDeploy.getStackType())),
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
}
