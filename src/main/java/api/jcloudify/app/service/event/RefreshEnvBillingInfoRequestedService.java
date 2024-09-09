package api.jcloudify.app.service.event;

import api.jcloudify.app.aws.cloudwatch.CloudwatchComponent;
import api.jcloudify.app.endpoint.event.model.RefreshEnvBillingInfoRequested;
import api.jcloudify.app.repository.model.Application;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.service.ApplicationService;
import api.jcloudify.app.service.EnvironmentService;
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
  private static final String LOG_INSIGHTS_QUERY_SUM_DURATION_AND_MEMORY =
      """
    fields @timestamp, @maxMemoryUsed, @duration
    | filter @message like /REPORT RequestId:/
    | stats\s
        sum(@billedDuration) as totalDurationInMs,
        sum(@maxMemoryUsed)/ 1048576 as totalMemoryMB
    """;
  private static final String LOG_GROUP_NAME_PATTERN_FOR_FRONTAL_FUNCTION =
      "%s-compute-%s-FrontalFunction";
  private static final String LOG_GROUP_NAME_PATTERN_FOR_WORKER_FUNCTIONS =
      "%s-compute-%s-WorkerFunction";
  private final CloudwatchComponent cloudwatchComponent;
  private final ApplicationService applicationService;
  private final EnvironmentService environmentService;

  @Override
  public void accept(RefreshEnvBillingInfoRequested rebirEvent) {
    var app = applicationService.getById(rebirEvent.getAppId());
    var env = environmentService.getById(rebirEvent.getEnvId());
    Instant startTime = rebirEvent.getPricingCalculationRequestStartTime();
    Instant endTime = rebirEvent.getPricingCalculationRequestEndTime();
    var logGroups = getLogGroupNamesBetweenDatesRangeInclusive(app, env, startTime, endTime);
    var queryId =
        cloudwatchComponent.initiateLogInsightsQuery(
            LOG_INSIGHTS_QUERY_SUM_DURATION_AND_MEMORY, startTime, endTime, logGroups);
    log.info("query {} initiated", queryId);
    // save queryId and send getResult event
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
    return getLogGroupNamesFilteredByCreationTimeBetween(
        rawFrontalLogGroups, rawWorkerLogGroups, startTime, endTime);
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

  private List<String> getLogGroupNamesFilteredByCreationTimeBetween(
      List<LogGroup> frontalLogGroups,
      List<LogGroup> workerLogGroups,
      Instant startTime,
      Instant endTime) {
    var logGroups = new ArrayList<>(frontalLogGroups);
    logGroups.addAll(workerLogGroups);
    return logGroups.stream()
        .filter(
            lg -> {
              var ct = Instant.ofEpochMilli(lg.creationTime());
              return ct.compareTo(startTime) >= 0 && ct.compareTo(endTime) <= 0;
            })
        .map(LogGroup::logGroupName)
        .toList();
  }
}
