package api.jcloudify.app.repository.jpa.dao;

import api.jcloudify.app.repository.model.Application;
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
public class ApplicationDao {
  private final EntityManager entityManager;

  public List<Application> findAllByCriteria(String userId, String name, Pageable pageable) {
    assert userId != null;
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Application> query = builder.createQuery(Application.class);
    Root<Application> root = query.from(Application.class);
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.and(builder.equal(root.get("userId"), userId)));
    predicates.add(builder.and(builder.equal(root.get("archived"), false)));

    if (name != null) {
      String concatenatedContainNamePattern = "%" + name + "%";
      Predicate hasUserFirstName =
          builder.or(
              builder.like(builder.lower(root.get("name")), concatenatedContainNamePattern),
              builder.like(root.get("name"), concatenatedContainNamePattern));
      predicates.add(hasUserFirstName);
    }

    query
        .orderBy(builder.asc(root.get("name")))
        .orderBy(QueryUtils.toOrders(pageable.getSort(), root, builder))
        .where(predicates.toArray(new Predicate[0]));
    return entityManager
        .createQuery(query)
        .setFirstResult((pageable.getPageNumber()) * pageable.getPageSize())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
  }
}
