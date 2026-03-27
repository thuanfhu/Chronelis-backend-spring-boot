package com.devloopsx.chronelis.dto.request.file;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MultipleDeleteFileRequest {
	List<String> filePaths;
}
