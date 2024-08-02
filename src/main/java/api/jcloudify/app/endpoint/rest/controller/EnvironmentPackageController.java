package api.jcloudify.app.endpoint.rest.controller;

import api.jcloudify.app.endpoint.rest.model.EnvironmentType;
import api.jcloudify.app.model.exception.InternalServerErrorException;
import api.jcloudify.app.service.EnvironmentPackageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@AllArgsConstructor
@Slf4j
public class EnvironmentPackageController {
    private final EnvironmentPackageService environmentPackageService;

    @PostMapping("/application/package")
    public ResponseEntity<String> uploadPackage(@RequestParam("file") MultipartFile file, @RequestParam String environmentType) {
        EnvironmentType type = Enum.valueOf(EnvironmentType.class, environmentType);
        environmentPackageService.uploadZippedPackage(type, file.getOriginalFilename(), convertToFile(file));
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    private File convertToFile(MultipartFile multipartFile) {
        File tempFile;
        try {
            tempFile = File.createTempFile("", multipartFile.getName());
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(multipartFile.getBytes());
            }
        } catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
        return tempFile;
    }
}
