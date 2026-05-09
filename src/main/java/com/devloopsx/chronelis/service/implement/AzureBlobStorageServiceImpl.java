package com.devloopsx.chronelis.service.implement;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobStorageException;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.service.AzureBlobStorageService;
import com.devloopsx.chronelis.utils.FileUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AzureBlobStorageServiceImpl implements AzureBlobStorageService {

  BlobContainerClient blobContainerClient;
  FileUtils fileUtils;

  @NonFinal
  @Value("${azure.storage.container-name}")
  String defaultFolderName;

  @Override
  public String uploadSingleFile(MultipartFile file, String folderName) {
    fileUtils.validateFile(file);
    ensureContainerExists();

    String resolvedFolderName = resolveFolderName(folderName);

    String normalizedFileName = fileUtils.normalizeFileName(
      file.getOriginalFilename()
    );
    String key = resolvedFolderName + "/" + normalizedFileName;

    try {
      BlobClient blobClient = blobContainerClient.getBlobClient(key);
      blobClient.upload(file.getInputStream(), file.getSize(), true);
      return blobClient.getBlobUrl();
    } catch (BlobStorageException e) {
      if (e.getStatusCode() == 404) {
        throw new ApplicationException(
          ErrorCode.FILE_UPLOAD_FAILED,
          "Container Azure Blob khong ton tai: " +
            blobContainerClient.getBlobContainerName()
        );
      }
      if (e.getStatusCode() == 403) {
        throw new ApplicationException(
          ErrorCode.FILE_UPLOAD_FAILED,
          "Tai khoan Azure Blob khong du quyen upload vao container: " +
            blobContainerClient.getBlobContainerName()
        );
      }
      throw new ApplicationException(
        ErrorCode.FILE_UPLOAD_FAILED,
        "Tai tep len Azure Blob that bai: " + resolveAzureMessage(e)
      );
    } catch (IOException e) {
      throw new ApplicationException(
        ErrorCode.FILE_UPLOAD_FAILED,
        "Khong the doc du lieu file: " + e.getMessage()
      );
    }
  }

  @Override
  public String uploadFile(
    byte[] content,
    String fileName,
    String folderName,
    String contentType
  ) {
    if (content == null || content.length == 0) {
      throw new ApplicationException(
        ErrorCode.FILE_UPLOAD_FAILED,
        "Khong co du lieu tep de tai len"
      );
    }

    fileUtils.validateFileSize(content.length);
    ensureContainerExists();

    String resolvedFolderName = resolveFolderName(folderName);
    String normalizedFileName = fileUtils.normalizeFileName(fileName);
    String key = resolvedFolderName + "/" + normalizedFileName;

    try {
      BlobClient blobClient = blobContainerClient.getBlobClient(key);
      blobClient.upload(
        new ByteArrayInputStream(content),
        content.length,
        true
      );

      if (StringUtils.hasText(contentType)) {
        blobClient.setHttpHeaders(
          new BlobHttpHeaders().setContentType(contentType.trim())
        );
      }

      return blobClient.getBlobUrl();
    } catch (BlobStorageException e) {
      if (e.getStatusCode() == 404) {
        throw new ApplicationException(
          ErrorCode.FILE_UPLOAD_FAILED,
          "Container Azure Blob khong ton tai: " +
            blobContainerClient.getBlobContainerName()
        );
      }
      if (e.getStatusCode() == 403) {
        throw new ApplicationException(
          ErrorCode.FILE_UPLOAD_FAILED,
          "Tai khoan Azure Blob khong du quyen upload vao container: " +
            blobContainerClient.getBlobContainerName()
        );
      }
      throw new ApplicationException(
        ErrorCode.FILE_UPLOAD_FAILED,
        "Tai tep len Azure Blob that bai: " + resolveAzureMessage(e)
      );
    }
  }

  @Override
  public List<String> uploadMultipleFiles(
    List<MultipartFile> files,
    String folderName
  ) {
    List<String> urls = new ArrayList<>();
    String resolvedFolderName = resolveFolderName(folderName);
    for (MultipartFile file : files) {
      urls.add(uploadSingleFile(file, resolvedFolderName));
    }
    return urls;
  }

  @Override
  public void deleteSingleFile(String filePath) {
    fileUtils.validateFilePath(filePath);
    BlobClient blobClient = blobContainerClient.getBlobClient(filePath);
    if (!blobClient.exists()) {
      throw new ApplicationException(ErrorCode.FILE_NOT_FOUND);
    }
    blobClient.delete();
  }

  @Override
  public void deleteMultipleFiles(List<String> filePaths) {
    for (String filePath : filePaths) {
      deleteSingleFile(filePath);
    }
  }

  @Override
  public void moveSingleFile(String sourceKey, String destinationFolder) {
    fileUtils.validateFilePath(sourceKey);

    BlobClient sourceBlobClient = blobContainerClient.getBlobClient(sourceKey);
    if (!sourceBlobClient.exists()) {
      throw new ApplicationException(
        ErrorCode.FILE_NOT_FOUND,
        "File nguon khong ton tai: " + sourceKey
      );
    }

    String fileName = sourceKey.substring(sourceKey.lastIndexOf("/") + 1);
    String destinationKey = destinationFolder + "/" + fileName;

    BlobClient destinationBlobClient = blobContainerClient.getBlobClient(
      destinationKey
    );
    destinationBlobClient.copyFromUrl(sourceBlobClient.getBlobUrl());
    sourceBlobClient.delete();
  }

  @Override
  public void moveMultipleFiles(
    List<String> sourceKeys,
    String destinationFolder
  ) {
    for (String sourceKey : sourceKeys) {
      moveSingleFile(sourceKey, destinationFolder);
    }
  }

  @Override
  public void deleteOldSingleImageIfPresent(String oldAvatarUrl) {
    if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
      String filePath = extractFilePathFromBlobUrl(oldAvatarUrl);
      if (filePath != null) {
        deleteSingleFile(filePath);
      }
    }
  }

  @Override
  public String extractFilePathFromBlobUrl(String blobUrl) {
    String endpoint = blobContainerClient.getBlobContainerUrl();
    String containerPrefix = endpoint.endsWith("/") ? endpoint : endpoint + "/";
    if (blobUrl.startsWith(containerPrefix)) {
      return blobUrl.substring(containerPrefix.length());
    }
    return null;
  }

  private String resolveFolderName(String folderName) {
    if (StringUtils.hasText(folderName)) {
      return folderName.trim();
    }
    return defaultFolderName;
  }

  private void ensureContainerExists() {
    try {
      blobContainerClient.createIfNotExists();
    } catch (BlobStorageException e) {
      throw new ApplicationException(
        ErrorCode.FILE_UPLOAD_FAILED,
        "Khong the khoi tao container Azure Blob: " + resolveAzureMessage(e)
      );
    }
  }

  private String resolveAzureMessage(BlobStorageException exception) {
    String serviceMessage = exception.getServiceMessage();
    if (StringUtils.hasText(serviceMessage)) {
      return serviceMessage;
    }
    return exception.getMessage();
  }
}
