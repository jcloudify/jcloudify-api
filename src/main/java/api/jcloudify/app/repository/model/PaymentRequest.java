package api.jcloudify.app.repository.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PaymentRequest implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  private Instant requestInstant;

  @OneToMany(mappedBy = "parentId", cascade = CascadeType.ALL)
  private List<UserPaymentRequest> userPaymentRequest;
}
