package com.devloopsx.chronelis.dto.request.goal;

import com.devloopsx.chronelis.constant.GoalStatusType;
import com.devloopsx.chronelis.constant.GoalType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateGoalRequest {
    @Size(max = 200, message = "INVALID_REQUEST_DATA")
    String title;

    GoalType goalType;

    GoalStatusType status;

    @DecimalMin(value = "0.00", message = "INVALID_REQUEST_DATA")
    @DecimalMax(value = "100.00", message = "INVALID_REQUEST_DATA")
    BigDecimal progressPercent;
}
