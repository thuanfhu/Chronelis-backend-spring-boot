package com.devloopsx.chronelis.dto.response.taskcomment;

import com.devloopsx.chronelis.dto.response.common.UserSummaryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskCommentResponse {
    Long id;
    Long taskId;
    Long parentCommentId;
    UserSummaryResponse user;
    String content;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
