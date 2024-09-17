package api.jcloudify.app.repository.model;

import api.jcloudify.app.endpoint.rest.model.DeploymentProgressionEventStateEnum;
import api.jcloudify.app.repository.model.workflows.State;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "\"deployment_state\"")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
public class DeploymentProgression extends State<DeploymentProgressionEventStateEnum> {
  private String appEnvDeploymentId;
}
