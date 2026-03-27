package com.devloopsx.chronelis.dto.request.file;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SingleUploadFileRequest {
	@NotNull(message = "FILE_NOT_BLANK")
	MultipartFile file;

	@Builder.Default
	String folderName = "uploads";
}
