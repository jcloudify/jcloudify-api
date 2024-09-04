package api.jcloudify.app.repository.model;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static org.hibernate.type.SqlTypes.NAMED_ENUM;

import api.jcloudify.app.repository.model.enums.BillingInfoComputeStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "\"billing_info\"")
@EqualsAndHashCode
@ToString
public class BillingInfo {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @CreationTimestamp private Instant creationDatetime;

  private Instant computeDatetime;

  private String userId;
  private String appId;
  private String envId;
  private String queryId;
  private String pricingMethod;
  private BigDecimal computedPriceInUsd;
  private Integer computedDurationInMinutes;
  private Integer computedMemoryUsedInMo;

  @JdbcTypeCode(NAMED_ENUM)
  @Enumerated(STRING)
  private BillingInfoComputeStatus status;
}
