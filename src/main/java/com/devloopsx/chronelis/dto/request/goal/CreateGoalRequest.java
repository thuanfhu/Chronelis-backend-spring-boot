package com.devloopsx.chronelis.dto.request.goal;

import com.devloopsx.chronelis.constant.GoalStatusType;
import com.devloopsx.chronelis.constant.GoalType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateGoalRequest {
  @NotNull(message = "INVALID_REQUEST_DATA")
  Long projectId;

  @NotBlank(message = "INVALID_REQUEST_DATA")
  @Size(max = 200, message = "INVALID_REQUEST_DATA")
  String title;

  @NotNull(message = "INVALID_REQUEST_DATA")
  GoalType goalType;

  GoalStatusType status;

  @DecimalMin(value = "0.00", message = "INVALID_REQUEST_DATA")
  @DecimalMax(value = "100.00", message = "INVALID_REQUEST_DATA")
  BigDecimal progressPercent;

  String managerUserId;

  Long managerTeamId;
}
