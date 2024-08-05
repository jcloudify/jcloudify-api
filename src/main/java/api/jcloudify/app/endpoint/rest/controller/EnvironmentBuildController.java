package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.service.EnvironmentBuildService;
import java.io.File;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class EnvironmentBuildController {
  private final EnvironmentBuildService environmentPackageService;

  @PostMapping("/gh-repos/{repo_owner}/{repo_name}/build")
  public ResponseEntity<String> uploadPackage(
      @PathVariable String repo_owner,
      @PathVariable String repo_name,
      @RequestPart("build_file") MultipartFile file,
      @RequestParam(name = "environment_type") EnvironmentType environmentType) {
    environmentPackageService.uploadZippedBuildFile(
        environmentType, file.getOriginalFilename(), convertToFile(file));
    return new ResponseEntity<>("OK", HttpStatus.OK);
  }

  private File convertToFile(MultipartFile multipartFile) {
    try {
      File tempFile = File.createTempFile("", multipartFile.getOriginalFilename());
      multipartFile.transferTo(tempFile);
      return tempFile;
    } catch (IOException e) {
      throw new InternalServerErrorException(e);
    }
  }
}
