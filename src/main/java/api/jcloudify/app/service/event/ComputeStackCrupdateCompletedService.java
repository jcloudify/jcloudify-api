package api.jcloudify.app.service.event;

import api.jcloudify.app.aws.cloudformation.CloudformationComponent;
import api.jcloudify.app.endpoint.event.model.ComputeStackCrupdateCompleted;
import api.jcloudify.app.repository.model.ComputeStackResource;
import api.jcloudify.app.repository.model.Stack;
import api.jcloudify.app.service.ComputeStackResourceService;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudformation.model.StackResource;

@Service
@AllArgsConstructor
@Slf4j
public class ComputeStackCrupdateCompletedService
    implements Consumer<ComputeStackCrupdateCompleted> {
  private final CloudformationComponent cloudformationComponent;
  private final ComputeStackResourceService computeStackResourceService;

  private ComputeStackResource getStackLambdaFunctions(String stackName) {
    List<StackResource> stackResources = cloudformationComponent.getStackResources(stackName);
    String FRONTAL_FUNCTION_LOGICAL_RESOURCE_ID = "FrontalFunction";
    String frontalFunctionName =
        getPhysicalResourceId(stackResources, FRONTAL_FUNCTION_LOGICAL_RESOURCE_ID);
    String WORKER_FUNCTION_1_LOGICAL_RESOURCE_ID = "WorkerFunction1";
    String workerFunction1Name =
        getPhysicalResourceId(stackResources, WORKER_FUNCTION_1_LOGICAL_RESOURCE_ID);
    String WORKER_FUNCTION_2_LOGICAL_RESOURCE_ID = "WorkerFunction2";
    String workerFunction2Name =
        getPhysicalResourceId(stackResources, WORKER_FUNCTION_2_LOGICAL_RESOURCE_ID);
    return ComputeStackResource.builder()
        .frontalFunctionName(frontalFunctionName)
        .worker1FunctionName(workerFunction1Name)
        .worker2FunctionName(workerFunction2Name)
        .build();
  }

  private String getPhysicalResourceId(
      List<StackResource> stackResources, String targetLogicalResourceId) {
    return stackResources.stream()
        .filter(stackResource -> stackResource.logicalResourceId().equals(targetLogicalResourceId))
        .map(StackResource::physicalResourceId)
        .findFirst()
        .orElse(null);
  }

  private boolean checkIfAlreadySaved(
      List<ComputeStackResource> saved, ComputeStackResource toCheck) {
    saved.forEach(
        computeStackResource -> {
          computeStackResource.setId(null);
          computeStackResource.setCreationDatetime(null);
        });
    return saved.contains(toCheck);
  }

  @Override
  public void accept(ComputeStackCrupdateCompleted computeStackCrupdateCompleted) {
    Stack crupdatedStack = computeStackCrupdateCompleted.getCrupdatedComputeStack();
    String stackName = crupdatedStack.getName();
    ComputeStackResource computeStackResource = getStackLambdaFunctions(stackName);
    computeStackResource.setEnvironmentId(crupdatedStack.getEnvironmentId());
    List<ComputeStackResource> saved =
        computeStackResourceService.findAllByEnvironmentId(crupdatedStack.getEnvironmentId());
    if (!checkIfAlreadySaved(saved, computeStackResource)) {
      log.info("Saving stack name={} compute resources name", stackName);
      computeStackResourceService.save(computeStackResource);
    }
  }
}