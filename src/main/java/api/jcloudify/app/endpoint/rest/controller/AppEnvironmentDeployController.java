package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.model.FileUploadRequestResponse;
import api.jcloudify.app.service.EnvironmentBuildService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AppEnvironmentDeployController {
  private final EnvironmentBuildService environmentBuildService;

  @GetMapping("/gh-repos/{repo_owner}/{repo_name}/upload-build-uri")
  public FileUploadRequestResponse createFileUploadUri(
      @PathVariable("repo_owner") String repoOwner,
      @PathVariable("repo_name") String repoName,
      @RequestParam(name = "environment_type") EnvironmentType environmentType) {
    return environmentBuildService.getZippedBuildUploadRequestDetails(environmentType);
  }

  @PutMapping("/gh-repos/{repo_owner}/{repo_name}/env-deploys")
  public String deployEnv(
      @PathVariable("repo_owner") String repoOwner,
      @PathVariable("repo_name") String repoName,
      @RequestParam(name = "environment_type") EnvironmentType environmentType,
      @RequestBody BuiltEnvInfo builtEnvInfo) {
    environmentBuildService.initiateDeployment(builtEnvInfo);
    return "ok";
  }
}
