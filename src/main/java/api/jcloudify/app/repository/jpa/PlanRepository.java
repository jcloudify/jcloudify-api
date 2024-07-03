package api.jcloudify.app.repository.jpa;

import api.jcloudify.app.repository.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, String> {
}
