package com.devloopsx.chronelis.service;

import com.devloopsx.chronelis.domain.*;

public interface CollaborationAccessService {
    Workspace requireWorkspace(Long workspaceId);

    WorkspaceMember requireWorkspaceMember(Long workspaceId, String userId);

    WorkspaceMember requireCurrentWorkspaceMember(Long workspaceId);

    void ensureCurrentUserIsWorkspaceManager(Long workspaceId);

    void ensureCurrentUserIsWorkspaceOwner(Long workspaceId);

    Project requireProject(Long projectId);

    Goal requireGoal(Long goalId);

    TaskStatus requireTaskStatus(Long statusId);

    Task requireTask(Long taskId);

    WorkspaceTeam requireWorkspaceTeam(Long teamId);

    void ensureCurrentUserCanAccessProject(Long projectId);

    void ensureCurrentUserCanManageProject(Long projectId);

    void ensureCurrentUserCanManageGoal(Long goalId);

    void ensureCurrentUserCanManageTask(Long taskId);

    void ensureCurrentUserCanAccessTask(Long taskId);

    void ensureAssigneeBelongsWorkspace(String assigneeId, Long workspaceId);
}
