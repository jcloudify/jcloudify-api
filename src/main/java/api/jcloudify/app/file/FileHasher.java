package api.jcloudify.app.file;

import static api.jcloudify.app.file.FileHashAlgorithm.SHA256;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.function.BiFunction;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class FileHasher implements BiFunction<File, FileHashAlgorithm, FileHash> {

  // Helper method to convert byte array to hexadecimal string
  private static String byteArrayToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      // Convert each byte to a two-digit hex string
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  @Override
  @SneakyThrows
  public FileHash apply(File file, FileHashAlgorithm fileHashAlgorithm) {
    if (!SHA256.equals(fileHashAlgorithm)) {
      throw new UnsupportedOperationException();
    }
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
      byte[] buffer = new byte[8192]; // 8 KB buffer
      int bytesRead;
      while ((bytesRead = bis.read(buffer)) != -1) {
        digest.update(buffer, 0, bytesRead);
      }
    }
    return new FileHash(fileHashAlgorithm, byteArrayToHex(digest.digest()));
  }
}
