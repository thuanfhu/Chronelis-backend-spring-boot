package com.devloopsx.chronelis.constant;

public enum EffectiveProjectAccessRoleType {
    NO_ACCESS,
    VIEWER,
    CONTRIBUTOR,
    MANAGER;

    public boolean atLeast(EffectiveProjectAccessRoleType requiredRole) {
        return this.ordinal() >= requiredRole.ordinal();
    }
}
