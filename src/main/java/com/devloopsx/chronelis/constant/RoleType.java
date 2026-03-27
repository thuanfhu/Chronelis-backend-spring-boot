package com.devloopsx.chronelis.constant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum RoleType {
	CUSTOMER_ROLE("CUSTOMER", "Khách hàng - Có thể duyệt, tìm kiếm và thuê sản phẩm"), STAFF_ROLE("STAFF",
			"Nhân viên - Có thể quản lý kho, giao nhận và hỗ trợ khách hàng"), ADMIN_ROLE("ADMIN",
					"Quản trị viên toàn quyền sử dụng hệ thống"),

	;

	String name;
	String description;

	public static List<String> getAllRoleNames() {
		return Arrays.stream(values()).map(RoleType::getName).collect(Collectors.toList());
	}
}
