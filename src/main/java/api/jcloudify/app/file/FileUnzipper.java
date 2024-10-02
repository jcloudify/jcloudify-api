package api.jcloudify.app.file;

import api.jcloudify.app.model.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FileUnzipper implements BiFunction<ZipFile, Path, Path> {
  private final FileWriter fileWriter;

  @Override
  public Path apply(ZipFile zipFile, Path extractDirectoryPath) {
    try {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory()) {
          try (InputStream is = zipFile.getInputStream(entry)) {
            String entryParentPath = getFolderPath(entry);
            String entryFilename = getFilename(entry);
            Path destinationPath = extractDirectoryPath.resolve(entryParentPath);
            byte[] bytes = is.readAllBytes();
            fileWriter.write(bytes, destinationPath.toFile(), entryFilename);
          }
        }
      }
      return extractDirectoryPath;
    } catch (IOException e) {
      throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION, e);
    }
  }

  private static String getFolderPath(ZipEntry zipEntry) {
    String entryPath = zipEntry.getName();
    Path normalizedPath = Paths.get(entryPath).normalize();

    validatePath(normalizedPath);

    Path parentPath = normalizedPath.getParent();
    return (parentPath != null) ? parentPath.toString() : "";
  }

  private static void validatePath(Path normalizedPath) {
    if (normalizedPath.startsWith("..")) {
      throw new IllegalArgumentException("Path traversal attempt detected");
    }
  }

  private static String getFilename(ZipEntry zipEntry) {
    String entryPath = zipEntry.getName();
    Path normalizedPath = Paths.get(entryPath).getFileName().normalize();

    validatePath(normalizedPath);

    return normalizedPath.toString();
  }
}
