package com.devloopsx.chronelis.dto.response.file;

import com.devloopsx.chronelis.dto.response.common.AuditResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MultipleFileResponse extends AuditResponse {

  List<SingleFileResponse> files;
}
