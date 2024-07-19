package api.jcloudify.app.endpoint.rest.mapper;

import api.jcloudify.app.endpoint.rest.model.PojaVersion;
import org.springframework.stereotype.Component;

@Component
public class PojaVersionMapper {
  public PojaVersion toRest(api.jcloudify.app.model.PojaVersion domain) {
    return new PojaVersion()
        .major(domain.getMajor())
        .minor(domain.getMinor())
        .patch(domain.getPatch())
        .humanReadableValue(domain.toHumanReadableValue());
  }
}
