package com.devloopsx.chronelis.dto.response.realtime;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
