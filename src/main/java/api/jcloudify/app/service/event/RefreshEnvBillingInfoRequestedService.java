package api.jcloudify.app.service.event;

import static api.jcloudify.app.repository.model.enums.BillingInfoComputeStatus.PENDING;

import api.jcloudify.app.aws.cloudwatch.CloudwatchComponent;
import api.jcloudify.app.endpoint.event.EventProducer;
import api.jcloudify.app.endpoint.event.model.GetBillingInfoQueryResultRequested;
import api.jcloudify.app.endpoint.event.model.RefreshEnvBillingInfoRequested;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.BillingInfo;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.BillingInfoService;
import api.jcloudify.app.service.EnvironmentService;
import api.jcloudify.app.service.UserService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

@Component
@AllArgsConstructor
@Slf4j
public class RefreshEnvBillingInfoRequestedService
    implements Consumer<RefreshEnvBillingInfoRequested> {
  private static final String LOG_INSIGHTS_QUERY_TOTAL_MEMORY_DURATION =
      """

fields @timestamp, @billedDuration/60000 as durationInMinutes, @memorySize/(1000000) as memorySizeInMo
 | filter @message like /REPORT RequestId:/
 | stats sum(durationInMinutes * memorySizeInMo) as billedMemoryDurationGrouped, sum(durationInMinutes) as billedDurationGrouped by memorySizeInMo
 | stats sum(billedMemoryDurationGrouped) as billedMemoryDuration, sum(billedDurationGrouped) as computedBilledDuration
""";
  private static final String LOG_GROUP_NAME_PATTERN_FOR_FRONTAL_FUNCTION =
      "%s-compute-%s-FrontalFunction";
  private static final String LOG_GROUP_NAME_PATTERN_FOR_WORKER_FUNCTIONS =
      "%s-compute-%s-WorkerFunction";
  private final CloudwatchComponent cloudwatchComponent;
  private final ApplicationService applicationService;
  private final EnvironmentService environmentService;
  private final BillingInfoService billingInfoService;
  private final UserService userService;
  private final EventProducer<GetBillingInfoQueryResultRequested> eventProducer;

  @Override
  public void accept(RefreshEnvBillingInfoRequested rebirEvent) {
    var app = applicationService.getById(rebirEvent.getAppId());
    var env = environmentService.getById(rebirEvent.getEnvId());
    Instant startTime = rebirEvent.getPricingCalculationRequestStartTime();
    Instant endTime = rebirEvent.getPricingCalculationRequestEndTime();
    var logGroups = getLogGroupNamesBetweenDatesRangeInclusive(app, env, startTime, endTime);
    var queryId =
        cloudwatchComponent.initiateLogInsightsQuery(
            LOG_INSIGHTS_QUERY_TOTAL_MEMORY_DURATION, startTime, endTime, logGroups);
    log.info("query {} initiated", queryId);
    var user = userService.getUserById(app.getUserId());
    var billingInfoToSave =
        BillingInfo.builder()
            .queryId(queryId)
            .userId(user.getId())
            .appId(app.getId())
            .envId(env.getId())
            .pricingMethod(user.getPricingMethod())
            .status(PENDING)
            .build();
    var savedBillingInfo = billingInfoService.crupdateBillingInfo(billingInfoToSave);
    log.info("Successfully initiated calculation for billing info {}", savedBillingInfo.getId());
    eventProducer.accept(List.of(new GetBillingInfoQueryResultRequested(queryId)));
  }

  private static String formatEnvTypeToLogGroupPattern(Environment env) {
    return env.getEnvironmentType().name().toLowerCase();
  }

  private static String formatFrontalFunctionLogGroupNamePattern(Application app, Environment env) {
    return LOG_GROUP_NAME_PATTERN_FOR_FRONTAL_FUNCTION.formatted(
        formatEnvTypeToLogGroupPattern(env), app.getName());
  }

  private static String formatWorkerFunctionLogGroupNamePattern(Application app, Environment env) {
    return LOG_GROUP_NAME_PATTERN_FOR_WORKER_FUNCTIONS.formatted(
        formatEnvTypeToLogGroupPattern(env), app.getName());
  }

  private List<String> getLogGroupNamesBetweenDatesRangeInclusive(
      Application app, Environment env, Instant startTime, Instant endTime) {
    String frontalFunctionLogGroupNamePattern = formatFrontalFunctionLogGroupNamePattern(app, env);
    List<LogGroup> rawFrontalLogGroups = getAllLogGroups(frontalFunctionLogGroupNamePattern);
    String workerFunctionLogGroupNamePattern = formatWorkerFunctionLogGroupNamePattern(app, env);
    List<LogGroup> rawWorkerLogGroups = getAllLogGroups(workerFunctionLogGroupNamePattern);
    return mergeListsAndExtractLogGroupNames(rawFrontalLogGroups, rawWorkerLogGroups);
  }

  private List<LogGroup> getAllLogGroups(String namePattern) {
    var logGroupsIterator =
        cloudwatchComponent.getLambdaFunctionLogGroupsByNamePatternIterator(namePattern);
    var logGroups = new ArrayList<LogGroup>();
    while (logGroupsIterator.hasNext()) {
      DescribeLogGroupsResponse current = logGroupsIterator.next();
      logGroups.addAll(current.logGroups());
    }
    return logGroups;
  }

  private List<String> mergeListsAndExtractLogGroupNames(
      List<LogGroup> frontalLogGroups, List<LogGroup> workerLogGroups) {
    var logGroups = new ArrayList<>(frontalLogGroups);
    logGroups.addAll(workerLogGroups);
    return logGroups.stream().map(LogGroup::logGroupName).toList();
  }
}
