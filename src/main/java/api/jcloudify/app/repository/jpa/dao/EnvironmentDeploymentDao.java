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
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class EnvironmentDeploymentDao {
  private final EntityManager entityManager;

  public List<AppEnvironmentDeployment> findAllByCriteria(
      String userId,
      String appId,
      EnvironmentType envType,
      Instant startDatetime,
      Instant endDatetime,
      Pageable pageable) {
    assert userId != null;
    assert appId != null;
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<AppEnvironmentDeployment> query =
        builder.createQuery(AppEnvironmentDeployment.class);
    Root<AppEnvironmentDeployment> root = query.from(AppEnvironmentDeployment.class);
    Join<AppEnvironmentDeployment, Environment> envDeplEnvJoin = root.join("envId");

    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.and(builder.equal(root.get("userId"), userId)));
    predicates.add(builder.and(builder.equal(root.get("appId"), appId)));
    if (startDatetime != null) {
      predicates.add(
          builder.and(builder.greaterThanOrEqualTo(root.get("creation_datetime"), startDatetime)));
    }
    if (endDatetime != null) {
      predicates.add(
          builder.and(builder.lessThanOrEqualTo(root.get("creation_datetime"), endDatetime)));
    }
    predicates.add(builder.and(builder.equal(envDeplEnvJoin.get("is_archived"), false)));
    if (envType != null) {
      predicates.add(builder.and(builder.equal(envDeplEnvJoin.get("environment_type"), envType)));
    }

    query
        .orderBy(builder.asc(root.get("creation_datetime")))
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder))
        .where(predicates.toArray(new Predicate[0]));
    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}