package com.devloopsx.chronelis.controller.rest;

import com.devloopsx.chronelis.dto.request.file.*;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.file.MultipleFileResponse;
import com.devloopsx.chronelis.dto.response.file.SingleFileResponse;
import com.devloopsx.chronelis.service.S3FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/storage/aws-s3")
public class S3FileStorageController {

	S3FileStorageService s3FileStorageService;

	@PostMapping("/upload/single")
	public ApiResponse<SingleFileResponse> uploadSingleFile(@Valid SingleUploadFileRequest singleUploadFileRequest,
			HttpServletRequest servletRequest) {
		String folderName = singleUploadFileRequest.getFolderName();
		MultipartFile file = singleUploadFileRequest.getFile();

		String fileUrl = s3FileStorageService.uploadSingleFile(file, folderName);
		SingleFileResponse responseDto = new SingleFileResponse(file.getOriginalFilename(), fileUrl);

		return ApiResponse.<SingleFileResponse>builder().message("Tải lên tệp đơn thành công").data(responseDto)
				.meta(buildMetaInfo(servletRequest)).build();
	}

	@PostMapping("/upload/multiple")
	public ApiResponse<MultipleFileResponse> uploadMultipleFiles(
			@Valid MultipleUploadFileRequest multipleUploadFileRequest, HttpServletRequest servletRequest) {
		String folderName = multipleUploadFileRequest.getFolderName();
		List<MultipartFile> files = multipleUploadFileRequest.getFiles();

		List<String> fileUrls = s3FileStorageService.uploadMultipleFiles(files, folderName);
		List<SingleFileResponse> fileResponses = new ArrayList<>();
		for (int i = 0; i < files.size(); i++) {
			fileResponses.add(new SingleFileResponse(files.get(i).getOriginalFilename(), fileUrls.get(i)));
		}

		MultipleFileResponse multipleDto = new MultipleFileResponse(fileResponses);

		return ApiResponse.<MultipleFileResponse>builder().message("Tải lên nhiều tệp thành công").data(multipleDto)
				.meta(buildMetaInfo(servletRequest)).build();
	}

	@DeleteMapping("/delete/single")
	public ApiResponse<String> deleteSingleFile(@RequestParam("filePath") String filePath,
			HttpServletRequest servletRequest) {
		s3FileStorageService.deleteSingleFile(filePath);
		return ApiResponse.<String>builder().message("Xóa tệp thành công").meta(buildMetaInfo(servletRequest)).build();
	}

	@DeleteMapping("/delete/multiple")
	public ApiResponse<String> deleteMultipleFiles(@RequestBody MultipleDeleteFileRequest multipleDeleteFileRequest,
			HttpServletRequest servletRequest) {
		s3FileStorageService.deleteMultipleFiles(multipleDeleteFileRequest.getFilePaths());
		return ApiResponse.<String>builder().message("Xóa nhiều tệp thành công").meta(buildMetaInfo(servletRequest))
				.build();
	}

	@PutMapping("/move/single")
	public ApiResponse<String> moveSingleFile(@Valid SingleMoveFileRequest singleMoveFileRequest,
			HttpServletRequest servletRequest) {
		String sourceKey = singleMoveFileRequest.getSourceKey();
		String destinationFolder = singleMoveFileRequest.getDestinationFolder();
		s3FileStorageService.moveSingleFile(sourceKey, destinationFolder);

		return ApiResponse.<String>builder().message("Di chuyển tệp thành công")
				.data("Tệp đã được di chuyển từ: " + sourceKey + " đến thư mục: " + destinationFolder)
				.meta(buildMetaInfo(servletRequest)).build();
	}

	@PutMapping("/move/multiple")
	public ApiResponse<String> moveMultipleFiles(@RequestBody MultipleMoveFileRequest requestDto,
			HttpServletRequest servletRequest) {
		s3FileStorageService.moveMultipleFiles(requestDto.getSourceKeys(), requestDto.getDestinationFolder());

		return ApiResponse.<String>builder().message("Di chuyển nhiều tệp thành công")
				.data("Các tệp đã được di chuyển tới thư mục: " + requestDto.getDestinationFolder())
				.meta(buildMetaInfo(servletRequest)).build();
	}
}
