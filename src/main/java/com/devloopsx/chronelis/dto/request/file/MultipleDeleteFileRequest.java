package com.devloopsx.chronelis.dto.request.file;

import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class MultipleDeleteFileRequest {

  List<String> filePaths;
}
