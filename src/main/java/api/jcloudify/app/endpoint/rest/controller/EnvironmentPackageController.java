package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.service.EnvironmentPackageService;
import java.io.File;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
public class EnvironmentPackageController {
  private final EnvironmentPackageService environmentPackageService;

  @PostMapping("/application/package")
  public ResponseEntity<String> uploadPackage(
      @RequestPart("file") MultipartFile file, @RequestParam EnvironmentType environmentType) {
    environmentPackageService.uploadZippedPackage(
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
