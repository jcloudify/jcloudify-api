package api.jcloudify.app.repository.model.mapper;

import api.jcloudify.app.endpoint.rest.model.CrupdateEnvironment;
import api.jcloudify.app.model.exception.NotFoundException;
import api.jcloudify.app.repository.jpa.PlanRepository;
import api.jcloudify.app.repository.model.Environment;
import api.jcloudify.app.repository.model.Plan;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component("DomainEnvironmentMapper")
@AllArgsConstructor
public class EnvironmentMapper {
    private final PlanRepository planRepository;

    public Environment toDomain(String applicationId, CrupdateEnvironment rest) {
        return Environment.builder()
                .id(rest.getId())
                .environmentType(rest.getEnvironmentType())
                .archived(rest.getArchived())
                .plan(getPlanById(rest.getPlanId()))
                .applicationId(applicationId)
                .build();
    }

    private Plan getPlanById(String planId) {
        return planRepository.findById(planId).orElseThrow(() -> new NotFoundException("The plan with identifier " + planId + " is not found"));
    }
}
