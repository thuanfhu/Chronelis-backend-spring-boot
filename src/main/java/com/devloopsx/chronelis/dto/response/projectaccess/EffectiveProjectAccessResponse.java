package com.devloopsx.chronelis.dto.response.projectaccess;

import com.devloopsx.chronelis.constant.EffectiveProjectAccessRoleType;
import com.devloopsx.chronelis.constant.ProjectVisibilityType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EffectiveProjectAccessResponse {
    Long projectId;
    Long workspaceId;
    ProjectVisibilityType visibility;
    EffectiveProjectAccessRoleType effectiveRole;
    boolean workspaceOwner;
    boolean canViewProject;
    boolean canContribute;
    boolean canManageProjectWork;
    boolean canManageProjectAccess;
    boolean canManageManagerAccess;
    boolean canChangeVisibility;
    boolean canDeleteProject;
    boolean canAssignOthers;
    boolean canComment;
}
