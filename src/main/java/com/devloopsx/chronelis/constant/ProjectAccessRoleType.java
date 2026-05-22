package com.devloopsx.chronelis.constant;

public enum ProjectAccessRoleType {
  VIEWER(1),
  CONTRIBUTOR(2),
  MANAGER(3);

  private final int level;

  ProjectAccessRoleType(int level) {
    this.level = level;
  }

  public boolean atLeast(ProjectAccessRoleType requiredRole) {
    return this.level >= requiredRole.level;
  }
}
