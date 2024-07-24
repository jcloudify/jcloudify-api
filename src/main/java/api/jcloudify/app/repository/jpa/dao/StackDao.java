package api.jcloudify.app.repository.jpa.dao;

import api.jcloudify.app.repository.model.Stack;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class StackDao {
  private final EntityManager entityManager;

  public List<Stack> findAllByCriteria(
      String userId, String applicationId, String environmentId, Pageable pageable) {
    assert userId != null;
    assert applicationId != null;
    assert environmentId != null;
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Stack> query = builder.createQuery(Stack.class);
    Root<Stack> root = query.from(Stack.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.equal(root.get("applicationId"), applicationId));
    predicates.add(builder.equal(root.get("environmentId"), environmentId));

    query
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder))
        .where(predicates.toArray(new Predicate[0]));
    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}