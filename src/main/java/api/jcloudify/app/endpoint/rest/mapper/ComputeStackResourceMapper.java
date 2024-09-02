package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.ComputeStackResource;
import org.springframework.stereotype.Component;

@Component
public class ComputeStackResourceMapper {
    public ComputeStackResource toRest(api.jcloudify.app.repository.model.ComputeStackResource domain) {
        return new ComputeStackResource()
                .id(domain.getId())
                .environmentId(domain.getEnvironmentId())
                .frontalFunctionName(domain.getFrontalFunctionName())
                .worker1FunctionName(domain.getWorker1FunctionName())
                .worker2FunctionName(domain.getWorker2FunctionName());
    }
}
