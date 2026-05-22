package com.devloopsx.chronelis.constant;

public enum EffectiveProjectAccessRoleType {
  NO_ACCESS(0),
  VIEWER(1),
  CONTRIBUTOR(2),
  MANAGER(3);

  private final int level;

  EffectiveProjectAccessRoleType(int level) {
    this.level = level;
  }

  public boolean atLeast(EffectiveProjectAccessRoleType requiredRole) {
    return this.level >= requiredRole.level;
  }

  public static EffectiveProjectAccessRoleType max(
      EffectiveProjectAccessRoleType left, EffectiveProjectAccessRoleType right) {
    return left.level >= right.level ? left : right;
  }
}
