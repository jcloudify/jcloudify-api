package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.Plan;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper {
  public Plan toRest(api.jcloudify.app.repository.model.Plan domain) {
    return new Plan().id(domain.getId()).name(domain.getName()).cost(domain.getCost());
  }
}
