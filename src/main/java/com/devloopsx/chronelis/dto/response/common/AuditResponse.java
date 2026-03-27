package com.devloopsx.chronelis.dto.response.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AuditResponse {
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
	Instant createdAt;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
	Instant updatedAt;

	String createdBy;
	String updatedBy;
}
