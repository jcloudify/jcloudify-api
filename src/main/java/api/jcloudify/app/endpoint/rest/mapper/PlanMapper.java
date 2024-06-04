package api.jcloudify.app.endpoint.rest.mapper;

import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper {
  public api.jcloudify.app.endpoint.rest.model.Plan toRest(
      api.jcloudify.app.repository.model.Plan domain) {
    return new api.jcloudify.app.endpoint.rest.model.Plan()
        .id(domain.getId())
        .name(domain.getName())
        .monthlyCost(BigDecimal.valueOf(domain.getMonthlyCost()))
        .yearlyCost(BigDecimal.valueOf(domain.getYearlyCost()));
  }
}
