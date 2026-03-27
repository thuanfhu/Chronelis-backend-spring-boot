package com.devloopsx.chronelis.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3FileStorageService {
	String uploadSingleFile(MultipartFile file, String folderName);

	List<String> uploadMultipleFiles(List<MultipartFile> files, String folderName);

	void deleteSingleFile(String filePath);

	void deleteMultipleFiles(List<String> filePaths);

	void moveSingleFile(String sourceKey, String destinationFolder);

	void moveMultipleFiles(List<String> sourceKeys, String destinationFolder);

	void deleteOldSingleImageIfPresent(String oldAvatarUrl);

	String extractFilePathFromS3Url(String s3Url);
}
