package api.jcloudify.app.repository.jpa.dao;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.repository.model.AppEnvironmentDeployment;
import api.jcloudify.app.repository.model.Environment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class EnvironmentDeploymentDao {
  private final EntityManager entityManager;

  public List<AppEnvironmentDeployment> findAllByCriteria(
      String appId,
      EnvironmentType envType,
      Instant startDatetime,
      Instant endDatetime,
      Pageable pageable) {
    assert appId != null;
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AppEnvironmentDeployment> query =
        builder.createQuery(AppEnvironmentDeployment.class);
    Root<AppEnvironmentDeployment> root = query.from(AppEnvironmentDeployment.class);
    Join<AppEnvironmentDeployment, Environment> envDeplEnvJoin = root.join("env");

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.and(builder.equal(root.get("appId"), appId)));
    if (startDatetime != null) {
      predicates.add(
          builder.and(builder.greaterThanOrEqualTo(root.get("creationDatetime"), startDatetime)));
    }
    if (endDatetime != null) {
      predicates.add(
          builder.and(builder.lessThanOrEqualTo(root.get("creationDatetime"), endDatetime)));
    }
    predicates.add(builder.and(builder.equal(envDeplEnvJoin.get("archived"), false)));
    if (envType != null) {
      predicates.add(builder.and(builder.equal(envDeplEnvJoin.get("environmentType"), envType)));
    }
    query
        .where(predicates.toArray(new Predicate[0]))
        .orderBy(builder.asc(root.get("creationDatetime")));
    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
