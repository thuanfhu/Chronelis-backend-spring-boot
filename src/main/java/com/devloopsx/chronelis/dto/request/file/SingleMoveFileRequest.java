package com.devloopsx.chronelis.dto.request.file;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class SingleMoveFileRequest {

  @NotBlank(message = "EMPTY_SOURCE_KEY")
  String sourceKey;

  @NotBlank(message = "DESTINATION_FOLDER_EMPTY")
  String destinationFolder;
}
