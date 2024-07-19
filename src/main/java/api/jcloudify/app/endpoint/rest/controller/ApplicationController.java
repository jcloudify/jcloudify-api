package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.ApplicationMapper;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplicationsRequestBody;
import api.jcloudify.app.endpoint.rest.model.CrupdateApplicationsResponse;
import api.jcloudify.app.service.ApplicationService;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {
  private final ApplicationService applicationService;
  private final ApplicationMapper mapper;

  public ApplicationController(
      ApplicationService applicationService,
      @Qualifier("RestApplicationMapper") ApplicationMapper mapper) {
    this.applicationService = applicationService;
    this.mapper = mapper;
  }

  @PutMapping("/users/{userId}/applications")
  public CrupdateApplicationsResponse crupdateApplications(
      @PathVariable String userId, @RequestBody CrupdateApplicationsRequestBody toCrupdate) {
    var data =
        mapper.toRest(
            applicationService.saveApplications(Objects.requireNonNull(toCrupdate.getData())));
    return new CrupdateApplicationsResponse().data(data);
  }
}
