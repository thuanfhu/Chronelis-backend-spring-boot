package com.devloopsx.chronelis.utils;

import com.azure.storage.blob.BlobContainerClient;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileUtils {

  BlobContainerClient blobContainerClient;

  @NonFinal
  @Value("${azure.storage.container-name}")
  String containerName;

  @NonFinal
  @Value("${file.allowedTypes}")
  List<String> allowedFileTypes;

  @NonFinal
  @Value("${file.maxSize}")
  long maxFileSize;

  @NonFinal
  @Value("${file.minSize}")
  long minFileSize;

  public String normalizeFileName(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank())
      throw new ApplicationException(ErrorCode.INVALID_FILE_NAME);

    String extension = "";
    int dotIndex = originalFilename.lastIndexOf(".");
    if (dotIndex != -1) {
      extension = originalFilename.substring(dotIndex);
      originalFilename = originalFilename.substring(0, dotIndex);
    }

    String normalized = originalFilename.replaceAll("[^a-zA-Z0-9-_]", "-");
    String uuid = UUID.randomUUID().toString();

    return normalized + "-" + uuid + extension;
  }

  public void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) throw new ApplicationException(ErrorCode.FILE_NOT_FOUND);

    String contentType = resolveContentType(file);
    List<String> normalizedAllowedFileTypes =
        allowedFileTypes.stream()
            .filter(StringUtils::hasText)
            .map(type -> type.trim().toLowerCase(Locale.ROOT))
            .toList();
    if (!StringUtils.hasText(contentType) || !normalizedAllowedFileTypes.contains(contentType)) {
      throw new ApplicationException(
          ErrorCode.FILE_TYPE_NOT_ALLOWED,
          "Loai file khong duoc phep. Chi chap nhan cac dinh dang: "
              + String.join(", ", normalizedAllowedFileTypes));
    }

    validateFileSize(file.getSize());
  }

  private String resolveContentType(MultipartFile file) {
    String contentType = file.getContentType();
    if (StringUtils.hasText(contentType)
        && !"application/octet-stream".equalsIgnoreCase(contentType)) {
      return contentType.trim().toLowerCase(Locale.ROOT);
    }

    return MediaTypeFactory.getMediaType(file.getOriginalFilename())
        .map(mediaType -> mediaType.toString().toLowerCase(Locale.ROOT))
        .orElseGet(
            () ->
                StringUtils.hasText(contentType)
                    ? contentType.trim().toLowerCase(Locale.ROOT)
                    : null);
  }

  public void validateFileSize(long size) {
    if (size > maxFileSize) {
      throw new ApplicationException(
          ErrorCode.FILE_TOO_LARGE,
          "Kich thuoc file qua lon. Dung luong toi da duoc phep la "
              + (maxFileSize / (1024 * 1024))
              + " MB");
    }
    if (size < minFileSize) {
      throw new ApplicationException(
          ErrorCode.FILE_TOO_SMALL,
          "Kich thuoc file qua nho. Dung luong toi thieu phai la " + (minFileSize / 1024) + " KB");
    }
  }

  public void validateFilePath(String filePath) {
    String filePathRegex = "^[^/]+/.+\\.[a-zA-Z0-9]+/?$";
    if (filePath == null || !filePath.matches(filePathRegex))
      throw new ApplicationException(ErrorCode.INVALID_FILE_PATH);
  }

  public boolean isFileExists(String filePath) {
    return blobContainerClient.getBlobClient(filePath).exists();
  }
}
