package com.devloopsx.chronelis.utils;

import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileUtils {
	S3Client s3Client;

	@NonFinal
	@Value("${aws.s3.bucketName}")
	String BUCKET_NAME;

	@NonFinal
	@Value("${file.allowedTypes}")
	List<String> AWS_S3_FILE_ALLOWED_TYPES;

	@NonFinal
	@Value("${file.maxSize}")
	long AWS_S3_FILE_MAX_SIZE;

	@NonFinal
	@Value("${file.minSize}")
	long AWS_S3_FILE_MIN_SIZE;

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
		if (file == null || file.isEmpty())
			throw new ApplicationException(ErrorCode.FILE_NOT_FOUND);

		String contentType = file.getContentType();
		if (!AWS_S3_FILE_ALLOWED_TYPES.contains(contentType)) {
			throw new ApplicationException(ErrorCode.FILE_TYPE_NOT_ALLOWED,
					"Loại file không được phép. Chỉ chấp nhận các định dạng: "
							+ String.join(", ", AWS_S3_FILE_ALLOWED_TYPES));
		}

		validateFileSize(file.getSize());
	}

	public void validateFileSize(long size) {
		if (size > AWS_S3_FILE_MAX_SIZE) {
			throw new ApplicationException(ErrorCode.FILE_TOO_LARGE,
					"Kích thước file quá lớn. Dung lượng tối đa được phép là " + (AWS_S3_FILE_MAX_SIZE / (1024 * 1024))
							+ " MB");
		}
		if (size < AWS_S3_FILE_MIN_SIZE) {
			throw new ApplicationException(ErrorCode.FILE_TOO_SMALL,
					"Kích thước file quá nhỏ. Dung lượng tối thiểu phải là " + (AWS_S3_FILE_MIN_SIZE / 1024) + " KB");
		}
	}

	public void validateFilePath(String filePath) {
		String filePathRegex = "^[^/]+/.+\\.[a-zA-Z0-9]+/?$";
		if (filePath == null || !filePath.matches(filePathRegex))
			throw new ApplicationException(ErrorCode.INVALID_FILE_PATH);
	}

	public boolean isFileExists(String filePath) {
		try {
			HeadObjectRequest headObjectRequest = HeadObjectRequest.builder().bucket(BUCKET_NAME).key(filePath).build();
			s3Client.headObject(headObjectRequest);
			return true;
		} catch (S3Exception e) {
			if (e.awsErrorDetails().errorCode().equals("NoSuchKey"))
				return false;
			throw e;
		}
	}
}
