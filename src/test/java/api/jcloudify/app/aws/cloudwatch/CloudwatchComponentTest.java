package api.jcloudify.app.aws.cloudwatch;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetQueryResultsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.QueryStatus;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResultField;

@Disabled("run in local only")
class CloudwatchComponentTest {
  CloudwatchComponent cloudwatchComponent =
      new CloudwatchComponent(
          CloudWatchLogsClient.builder()
              .credentialsProvider(ProfileCredentialsProvider.create("jc"))
              .build());

  @Test
  void initiateLogInsightsQuery() {
    var beginTestTime = System.currentTimeMillis();
    Instant startTime = Instant.parse("2024-08-01T00:00:00Z");
    Instant endTime = Instant.parse("2024-08-31T23:59:59Z");
    String queryId =
        cloudwatchComponent.initiateLogInsightsQuery(
            """
fields @timestamp, @maxMemoryUsed, @duration
| filter @message like /REPORT RequestId:/
| stats\s
	sum(@duration)/ 60000 as totalDurationMinutes,
	sum(@maxMemoryUsed)/ 1048576 as totalMemoryMB
""",
            startTime,
            endTime,
            List.of("/aws/lambda/prod-compute-jcloudify-dovecot-FrontalFunction-64fjAtgl1WXU"));

    // Poll for query completion
    boolean isQueryComplete = false;
    while (!isQueryComplete) {
      try {
        Thread.sleep(1000); // Sleep for 1 second before checking again
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        e.printStackTrace();
      }

      GetQueryResultsResponse getQueryResultsResponse = cloudwatchComponent.getQueryResult(queryId);
      QueryStatus status = getQueryResultsResponse.status();

      if (status == QueryStatus.COMPLETE) {
        isQueryComplete = true;
        long testEndTime = System.currentTimeMillis(); // End timing
        long duration = testEndTime - beginTestTime;
        List<List<ResultField>> results = getQueryResultsResponse.results();
        List<ResultField> first = results.getFirst();
        assert first.size() == 2;
        var totalDurationMinutes = first.getFirst();
        assert "totalDurationMinutes".equals(totalDurationMinutes.field());
        var totalMemoryMb = first.getLast();
        assert "totalMemoryMB".equals(totalMemoryMb.field());
        System.out.println("Query completed in " + duration + " milliseconds.");
      } else if (status == QueryStatus.FAILED) {
        System.out.println("Query failed.");
        break;
      }
    }
  }
}
