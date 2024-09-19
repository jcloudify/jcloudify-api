package api.jcloudify.app.repository.model;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Comparator.comparing;

import api.jcloudify.app.repository.model.workflows.StateMachine;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "\"app_environment_deployment\"")
@EqualsAndHashCode
@ToString
public class AppEnvironmentDeployment implements StateMachine<DeploymentState> {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private String appId;

  @JoinColumn(name = "env_id")
  @ManyToOne
  private Environment env;

  private String envDeplConfId;
  private String deployedUrl;
  @CreationTimestamp private Instant creationDatetime;

  @OneToMany(cascade = ALL, fetch = EAGER, mappedBy = "appEnvDeploymentId")
  @Builder.Default
  private List<DeploymentState> states = new ArrayList<>();

  public String getGhCommitBranch() {
    return env.getFormattedEnvironmentType();
  }

  private String ghCommitMessage;
  private String ghCommitSha;
  private String ghCommitUrl;
  private String ghRepoName;
  private String ghRepoOwnerName;
  private String ghCommitterName;
  private String ghCommitterEmail;
  private String ghCommitterId;
  private String ghCommitterAvatarUrl;
  private String ghCommitterLogin;
  private String ghCommitterType;

  @Override
  public List<DeploymentState> getStates() {
    List<DeploymentState> states = this.states;
    states.sort(comparing(DeploymentState::getTimestamp).reversed());
    return states;
  }

  @Override
  public Optional<DeploymentState> getLatestState() {
    return this.getStates().isEmpty() ? Optional.empty() : Optional.of(getStates().getFirst());
  }

  @Override
  public List<DeploymentState> addState(DeploymentState newState) {
    List<DeploymentState> actualStates = this.getStates();
    var optionalLatestState = this.getLatestState();
    if (!actualStates.isEmpty() && optionalLatestState.isPresent()) {
      var latestState = optionalLatestState.get();
      var errorMessage =
          String.format("Illegal transition status from=%s to=%s", latestState, newState);
      if (newState.getTimestamp().isBefore(latestState.getTimestamp())) {
        throw new IllegalArgumentException(errorMessage);
      }
      actualStates.add(this.to(latestState, newState, errorMessage));
      return actualStates;
    }
    actualStates.add(newState);
    return actualStates;
  }

  private DeploymentState to(
      DeploymentState latestState, DeploymentState newState, String errorMessage) {
    var latestStatus = latestState.getProgressionStatus();
    var newStatus = newState.getProgressionStatus();
    switch (latestStatus) {
      case TEMPLATE_FILE_CHECK_IN_PROGRESS -> {
        switch (newStatus) {
          case TEMPLATE_FILE_CHECK_FAILED, INDEPENDENT_STACK_DEPLOYMENT_IN_PROGRESS -> {
            return newState;
          }
          default -> throw new IllegalArgumentException(errorMessage);
        }
      }
      case INDEPENDENT_STACK_DEPLOYMENT_IN_PROGRESS -> {
        switch (newStatus) {
          case INDEPENDENT_STACK_DEPLOYMENT_FAILED, COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS -> {
            return newState;
          }
          default -> throw new IllegalArgumentException(errorMessage);
        }
      }
      case COMPUTE_STACK_DEPLOYMENT_IN_PROGRESS -> {
        switch (newStatus) {
          case COMPUTE_STACK_DEPLOYMENT_FAILED, COMPUTE_STACK_DEPLOYED -> {
            return newState;
          }
          default -> throw new IllegalArgumentException(errorMessage);
        }
      }
      default -> throw new IllegalArgumentException(errorMessage);
    }
  }
}
