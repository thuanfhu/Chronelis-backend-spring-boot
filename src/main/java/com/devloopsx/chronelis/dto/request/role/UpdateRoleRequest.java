package com.devloopsx.chronelis.dto.request.role;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UpdateRoleRequest {
	String name;
	String description;
	Boolean active;
	List<String> permissionIds;
}
