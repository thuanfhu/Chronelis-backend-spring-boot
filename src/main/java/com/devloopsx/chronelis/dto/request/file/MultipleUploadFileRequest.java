package com.devloopsx.chronelis.dto.request.file;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MultipleUploadFileRequest {
	@NotNull(message = "FILE_NOT_BLANK")
	List<MultipartFile> files;

	@Builder.Default
	String folderName = "uploads";
}
