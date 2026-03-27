package com.devloopsx.chronelis.dto.request.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MultipleMoveFileRequest {
	@NotEmpty(message = "EMPTY_SOURCE_LIST")
	List<@NotBlank(message = "EMPTY_SOURCE_KEY") String> sourceKeys;

	@NotBlank(message = "DESTINATION_FOLDER_EMPTY")
	String destinationFolder;
}
