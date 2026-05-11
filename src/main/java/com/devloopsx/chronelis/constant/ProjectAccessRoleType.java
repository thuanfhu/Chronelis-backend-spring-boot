package com.devloopsx.chronelis.constant;

public enum ProjectAccessRoleType {
    VIEWER,
    CONTRIBUTOR,
    MANAGER;

    public boolean atLeast(ProjectAccessRoleType requiredRole) {
        return this.ordinal() >= requiredRole.ordinal();
    }
}
