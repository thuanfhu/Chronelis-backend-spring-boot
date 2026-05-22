package com.devloopsx.chronelis.dto.response.common;

import com.devloopsx.chronelis.exception.ErrorDetail;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // if field equal null, don't appear in body response
public class ApiResponse<T> {
  @Builder.Default Boolean success = true;

  String message; // success request
  T data; // success request

  List<ErrorDetail> errors; // fail request

  MetaInfo meta;
}
