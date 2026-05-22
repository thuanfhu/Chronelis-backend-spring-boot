package com.devloopsx.chronelis.dto.response.realtime;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RealtimeEventResponse {
  String eventType;
  Object data;
  LocalDateTime occurredAt;
}
