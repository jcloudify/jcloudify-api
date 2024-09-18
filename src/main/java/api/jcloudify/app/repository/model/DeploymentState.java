package api.jcloudify.app.repository.model;

import api.jcloudify.app.endpoint.rest.model.DeploymentStateEnum;
import api.jcloudify.app.repository.model.workflows.State;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@PrimaryKeyJoinColumn(name = "id")
@Entity
@Table(name = "\"deployment_state\"")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
@Setter
public class DeploymentState extends State<DeploymentStateEnum> {
  @JoinColumn(referencedColumnName = "id")
  private String appEnvDeploymentId;
}
