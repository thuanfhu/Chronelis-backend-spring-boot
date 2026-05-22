package com.devloopsx.chronelis.controller.rest;

import static com.devloopsx.chronelis.utils.MetaUtils.buildMetaInfo;

import com.devloopsx.chronelis.dto.request.file.MultipleDeleteFileRequest;
import com.devloopsx.chronelis.dto.request.file.MultipleMoveFileRequest;
import com.devloopsx.chronelis.dto.request.file.MultipleUploadFileRequest;
import com.devloopsx.chronelis.dto.request.file.SingleMoveFileRequest;
import com.devloopsx.chronelis.dto.request.file.SingleUploadFileRequest;
import com.devloopsx.chronelis.dto.response.common.ApiResponse;
import com.devloopsx.chronelis.dto.response.file.MultipleFileResponse;
import com.devloopsx.chronelis.dto.response.file.SingleFileResponse;
import com.devloopsx.chronelis.service.AzureBlobStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/v1/storage/azure-blob")
public class AzureBlobStorageController {

  AzureBlobStorageService azureBlobStorageService;

  @PostMapping("/upload/single")
  public ApiResponse<SingleFileResponse> uploadSingleFile(
      @Valid SingleUploadFileRequest singleUploadFileRequest, HttpServletRequest servletRequest) {
    String folderName = singleUploadFileRequest.getFolderName();
    MultipartFile file = singleUploadFileRequest.getFile();

    String fileUrl = azureBlobStorageService.uploadSingleFile(file, folderName);
    SingleFileResponse responseDto = new SingleFileResponse(file.getOriginalFilename(), fileUrl);

    return ApiResponse.<SingleFileResponse>builder()
        .message("Tai len tep don thanh cong")
        .data(responseDto)
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PostMapping("/upload/multiple")
  public ApiResponse<MultipleFileResponse> uploadMultipleFiles(
      @Valid MultipleUploadFileRequest multipleUploadFileRequest,
      HttpServletRequest servletRequest) {
    String folderName = multipleUploadFileRequest.getFolderName();
    List<MultipartFile> files = multipleUploadFileRequest.getFiles();

    List<String> fileUrls = azureBlobStorageService.uploadMultipleFiles(files, folderName);
    List<SingleFileResponse> fileResponses = new ArrayList<>();
    for (int i = 0; i < files.size(); i++) {
      fileResponses.add(
          new SingleFileResponse(files.get(i).getOriginalFilename(), fileUrls.get(i)));
    }

    MultipleFileResponse multipleDto = new MultipleFileResponse(fileResponses);

    return ApiResponse.<MultipleFileResponse>builder()
        .message("Tai len nhieu tep thanh cong")
        .data(multipleDto)
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @DeleteMapping("/delete/single")
  public ApiResponse<String> deleteSingleFile(
      @RequestParam("filePath") String filePath, HttpServletRequest servletRequest) {
    azureBlobStorageService.deleteSingleFile(filePath);
    return ApiResponse.<String>builder()
        .message("Xoa tep thanh cong")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @DeleteMapping("/delete/multiple")
  public ApiResponse<String> deleteMultipleFiles(
      @RequestBody MultipleDeleteFileRequest multipleDeleteFileRequest,
      HttpServletRequest servletRequest) {
    azureBlobStorageService.deleteMultipleFiles(multipleDeleteFileRequest.getFilePaths());
    return ApiResponse.<String>builder()
        .message("Xoa nhieu tep thanh cong")
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PutMapping("/move/single")
  public ApiResponse<String> moveSingleFile(
      @Valid SingleMoveFileRequest singleMoveFileRequest, HttpServletRequest servletRequest) {
    String sourceKey = singleMoveFileRequest.getSourceKey();
    String destinationFolder = singleMoveFileRequest.getDestinationFolder();
    azureBlobStorageService.moveSingleFile(sourceKey, destinationFolder);

    return ApiResponse.<String>builder()
        .message("Di chuyen tep thanh cong")
        .data("Tep da duoc di chuyen tu: " + sourceKey + " den thu muc: " + destinationFolder)
        .meta(buildMetaInfo(servletRequest))
        .build();
  }

  @PutMapping("/move/multiple")
  public ApiResponse<String> moveMultipleFiles(
      @RequestBody @Valid MultipleMoveFileRequest requestDto, HttpServletRequest servletRequest) {
    azureBlobStorageService.moveMultipleFiles(
        requestDto.getSourceKeys(), requestDto.getDestinationFolder());

    return ApiResponse.<String>builder()
        .message("Di chuyen nhieu tep thanh cong")
        .data("Cac tep da duoc di chuyen toi thu muc: " + requestDto.getDestinationFolder())
        .meta(buildMetaInfo(servletRequest))
        .build();
  }
}
