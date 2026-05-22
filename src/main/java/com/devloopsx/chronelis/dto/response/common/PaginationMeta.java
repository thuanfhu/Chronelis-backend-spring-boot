package com.devloopsx.chronelis.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginationMeta {
  int currentPage;
  int pageSize;
  int totalPages;
  long totalElements;
  boolean hasNext;
  boolean hasPrevious;
}
