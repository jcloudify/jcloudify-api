package api.jcloudify.app.repository.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Plan {
  @Id private String id;
  private String name;
  private double cost;
}
