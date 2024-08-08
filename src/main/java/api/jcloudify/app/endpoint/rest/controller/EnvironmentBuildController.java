package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.model.FileUploadRequestResponse;
import api.jcloudify.app.service.EnvironmentBuildService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class EnvironmentBuildController {
  private final EnvironmentBuildService environmentPackageService;

  @GetMapping("/gh-repos/{repo_owner}/{repo_name}/upload-build-uri")
  public FileUploadRequestResponse createFileUploadUri(
      @PathVariable("repo_owner") String repoOwner,
      @PathVariable("repo_name") String repoName,
      @RequestParam(name = "environment_type") EnvironmentType environmentType) {
    return environmentPackageService.getZippedBuildUploadRequestDetails(environmentType);
  }
}
