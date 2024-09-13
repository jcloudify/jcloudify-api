package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.BuildUploadRequestResponse;
import api.jcloudify.app.endpoint.rest.model.BuiltEnvInfo;
import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.endpoint.rest.security.model.ApplicationPrincipal;
import api.jcloudify.app.service.EnvironmentBuildService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GhAppEnvironmentDeployController {
  private final EnvironmentBuildService environmentBuildService;

  @GetMapping("/gh-repos/{repo_owner}/{repo_name}/upload-build-uri")
  public BuildUploadRequestResponse createFileUploadUri(
      @PathVariable("repo_owner") String repoOwner,
      @PathVariable("repo_name") String repoName,
      @RequestParam(name = "environment_type") EnvironmentType environmentType) {
    return environmentBuildService.getZippedBuildUploadRequestDetails(environmentType);
  }

  @PutMapping("/gh-repos/{repo_owner}/{repo_name}/env-deploys")
  public BuiltEnvInfo deployEnv(
      @PathVariable("repo_owner") String repoOwner,
      @PathVariable("repo_name") String repoName,
      @RequestParam(name = "environment_type") EnvironmentType environmentType,
      @AuthenticationPrincipal ApplicationPrincipal principal,
      @RequestBody BuiltEnvInfo payload) {
    environmentBuildService.initiateDeployment(
        repoOwner, repoName, principal.getInstallationId(), payload);
    return payload;
  }
}
