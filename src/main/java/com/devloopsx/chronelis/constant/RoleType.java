package com.devloopsx.chronelis.constant;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum RoleType {
  USER_ROLE("USER", "Người dùng - Sử dụng đầy đủ chức năng cộng tác và quản lý dự án"),
  ADMIN_ROLE(
      "ADMIN", "Quản trị viên - Quản lý toàn bộ hệ thống bao gồm người dùng, vai trò và quyền"),
  ;

  String name;
  String description;

  public static List<String> getAllRoleNames() {
    return Arrays.stream(values()).map(RoleType::getName).collect(Collectors.toList());
  }
}
