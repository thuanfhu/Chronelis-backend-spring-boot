package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.service.S3FileStorageService;
import com.devloopsx.chronelis.utils.FileUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class S3FileStorageServiceImpl implements S3FileStorageService {
	S3Client s3Client;
	FileUtils fileUtils;

	@NonFinal
	@Value("${aws.s3.bucketName}")
	String BUCKET_NAME;

	@Override
	public String uploadSingleFile(MultipartFile file, String folderName) {
		this.fileUtils.validateFile(file);

		String normalizedFileName = this.fileUtils.normalizeFileName(file.getOriginalFilename());
		String key = folderName + "/" + normalizedFileName;

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(BUCKET_NAME).key(key)
					.contentType(file.getContentType()).build();

			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		} catch (IOException e) {
			throw new RuntimeException("Không thể đọc dữ liệu file: " + e.getMessage(), e);
		}

		return "https://" + BUCKET_NAME + ".s3.amazonaws.com/" + key;
	}

	@Override
	public List<String> uploadMultipleFiles(List<MultipartFile> files, String folderName) {
		List<String> urls = new ArrayList<>();
		for (MultipartFile file : files) {
			String url = uploadSingleFile(file, folderName);
			urls.add(url);
		}
		return urls;
	}

	@Override
	public void deleteSingleFile(String filePath) {
		this.fileUtils.validateFilePath(filePath);
		if (!this.fileUtils.isFileExists(filePath))
			throw new ApplicationException(ErrorCode.FILE_NOT_FOUND);

		DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder().bucket(BUCKET_NAME).key(filePath).build();
		this.s3Client.deleteObject(deleteRequest);
	}

	@Override
	public void deleteMultipleFiles(List<String> filePaths) {
		for (String filePath : filePaths) {
			deleteSingleFile(filePath);
		}
	}

	@Override
	public void moveSingleFile(String sourceKey, String destinationFolder) {
		this.fileUtils.validateFilePath(sourceKey);
		if (!this.fileUtils.isFileExists(sourceKey))
			throw new ApplicationException(ErrorCode.FILE_NOT_FOUND, "File nguồn không tồn tại: " + sourceKey);

		String fileName = sourceKey.substring(sourceKey.lastIndexOf("/") + 1);
		String destinationKey = destinationFolder + "/" + fileName;

		CopyObjectRequest copyRequest = CopyObjectRequest.builder().sourceBucket(BUCKET_NAME).sourceKey(sourceKey)
				.destinationBucket(BUCKET_NAME).destinationKey(destinationKey).build();
		s3Client.copyObject(copyRequest);

		deleteSingleFile(sourceKey);
	}

	@Override
	public void moveMultipleFiles(List<String> sourceKeys, String destinationFolder) {
		for (String sourceKey : sourceKeys) {
			moveSingleFile(sourceKey, destinationFolder);
		}
	}

	public void deleteOldSingleImageIfPresent(String oldAvatarUrl) {
		if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
			String filePath = this.extractFilePathFromS3Url(oldAvatarUrl);
			if (filePath != null) {
				this.deleteSingleFile(filePath);
			}
		}
	}

	public String extractFilePathFromS3Url(String s3Url) {
		String bucketPrefix = "https://" + BUCKET_NAME + ".s3.amazonaws.com/";
		if (s3Url.startsWith(bucketPrefix))
			return s3Url.substring(bucketPrefix.length());
		return null;
	}
}
