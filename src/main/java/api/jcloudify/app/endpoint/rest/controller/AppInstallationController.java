package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.mapper.AppInstallationMapper;
import api.jcloudify.app.endpoint.rest.model.CrupdateGithubAppInstallationsRequestBody;
import api.jcloudify.app.endpoint.rest.model.CrupdateGithubAppInstallationsResponse;
import api.jcloudify.app.endpoint.rest.model.GithubAppInstallationsResponse;
import api.jcloudify.app.service.AppInstallationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AppInstallationController {
  private final AppInstallationService service;
  private final AppInstallationMapper mapper;

  @GetMapping("/users/{userId}/installations")
  public GithubAppInstallationsResponse getUserInstallations(@PathVariable String userId) {
    var data = service.findAllByUserId(userId).stream().map(mapper::toRest).toList();
    return new GithubAppInstallationsResponse().data(data);
  }

  @PutMapping("/users/{userId}/installations")
  public CrupdateGithubAppInstallationsResponse crupdateGithubAppInstallations(
      @PathVariable String userId,
      @RequestBody CrupdateGithubAppInstallationsRequestBody requestBody) {
    var requestBodyData = requestBody.getData();
    var savedData =
        service.saveAll(
            requestBodyData.stream().map((rest) -> mapper.toDomain(userId, rest)).toList());
    var data = savedData.stream().map(mapper::toRest).toList();
    return new CrupdateGithubAppInstallationsResponse().data(data);
  }
}
