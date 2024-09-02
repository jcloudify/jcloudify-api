package api.jcloudify.app.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "\"compute_resources\"")
@EqualsAndHashCode
@ToString
public class ComputeStackResource {
    @Id
    private String id;
    private String environmentId;
    private String frontalFunctionName;
    @Column(name = "worker_1_function_name")
    private String worker1FunctionName;
    @Column(name = "worker_2_function_name")
    private String worker2FunctionName;
    @CreationTimestamp
    private Instant creationDatetime;
}
