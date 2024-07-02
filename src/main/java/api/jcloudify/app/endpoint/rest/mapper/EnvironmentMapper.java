package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.Environment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EnvironmentMapper {
    private final PlanMapper planMapper;

    public Environment toRest(api.jcloudify.app.repository.model.Environment domain) {
        return new Environment()
                .id(domain.getId())
                .state(domain.getState())
                .plan(planMapper.toRest(domain.getPlan()))
                .environmentType(domain.getEnvironmentType());
    }
}
