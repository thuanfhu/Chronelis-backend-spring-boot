package com.devloopsx.chronelis.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface AzureBlobStorageService {
  String uploadSingleFile(MultipartFile file, String folderName);

  String uploadFile(byte[] content, String fileName, String folderName, String contentType);

  List<String> uploadMultipleFiles(List<MultipartFile> files, String folderName);

  void deleteSingleFile(String filePath);

  void deleteMultipleFiles(List<String> filePaths);

  void moveSingleFile(String sourceKey, String destinationFolder);

  void moveMultipleFiles(List<String> sourceKeys, String destinationFolder);

  void deleteOldSingleImageIfPresent(String oldAvatarUrl);

  String extractFilePathFromBlobUrl(String blobUrl);
}
