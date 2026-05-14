package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.EffectiveProjectAccessRoleType;
import com.devloopsx.chronelis.domain.*;
import com.devloopsx.chronelis.dto.response.projectaccess.EffectiveProjectAccessResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.*;
import com.devloopsx.chronelis.service.CollaborationAccessService;
import com.devloopsx.chronelis.service.ProjectPermissionService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CollaborationAccessServiceImpl implements CollaborationAccessService {
    WorkspaceRepository workspaceRepository;
    WorkspaceMemberRepository workspaceMemberRepository;
    WorkspaceTeamRepository workspaceTeamRepository;
    WorkspaceTeamMemberRepository workspaceTeamMemberRepository;
    ProjectRepository projectRepository;
    GoalRepository goalRepository;
    TaskStatusRepository taskStatusRepository;
    TaskRepository taskRepository;
    UserRepository userRepository;
    SecurityUtils securityUtils;
    ProjectPermissionService projectPermissionService;

    @Override
    public Workspace requireWorkspace(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Workspace không tồn tại"));
    }

    @Override
    public WorkspaceMember requireWorkspaceMember(Long workspaceId, String userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserUserId(workspaceId, userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                        "Người dùng không thuộc workspace này"));
    }

    @Override
    public WorkspaceMember requireCurrentWorkspaceMember(Long workspaceId) {
        User currentUser = securityUtils.getAuthenticatedUser();
        return requireWorkspaceMember(workspaceId, currentUser.getUserId());
    }

    @Override
    public void ensureCurrentUserIsWorkspaceManager(Long workspaceId) {
        ensureCurrentUserIsWorkspaceOwner(workspaceId);
    }

    @Override
    public void ensureCurrentUserIsWorkspaceOwner(Long workspaceId) {
        User currentUser = securityUtils.getAuthenticatedUser();
        WorkspaceMember member = requireWorkspaceMember(workspaceId, currentUser.getUserId());

        if (member.getRole() != com.devloopsx.chronelis.constant.WorkspaceMemberRoleType.OWNER) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Chỉ owner workspace mới có quyền thực hiện thao tác này");
        }
    }

    @Override
    public Project requireProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Project không tồn tại"));
    }

    @Override
    public Goal requireGoal(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Goal không tồn tại"));
    }

    @Override
    public TaskStatus requireTaskStatus(Long statusId) {
        return taskStatusRepository.findById(statusId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Task status không tồn tại"));
    }

    @Override
    public Task requireTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Task không tồn tại"));
    }

    @Override
    public WorkspaceTeam requireWorkspaceTeam(Long teamId) {
        return workspaceTeamRepository.findById(teamId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Team không tồn tại"));
    }

    @Override
    public void ensureCurrentUserCanAccessProject(Long projectId) {
        Project project = requireProject(projectId);
        requireMinimumProjectRole(project, EffectiveProjectAccessRoleType.VIEWER,
                "Bạn không có quyền truy cập project này");
    }

    @Override
    public void ensureCurrentUserCanManageProject(Long projectId) {
        ensureCurrentUserCanManageProjectWork(projectId);
    }

    @Override
    public void ensureCurrentUserCanContributeToProject(Long projectId) {
        Project project = requireProject(projectId);
        requireMinimumProjectRole(project, EffectiveProjectAccessRoleType.CONTRIBUTOR,
                "Bạn không có quyền thao tác trong project này");
    }

    @Override
    public void ensureCurrentUserCanManageProjectWork(Long projectId) {
        Project project = requireProject(projectId);
        requireMinimumProjectRole(project, EffectiveProjectAccessRoleType.MANAGER,
                "Bạn không có quyền quản lý công việc trong project này");
    }

    @Override
    public void ensureCurrentUserCanManageProjectAccess(Long projectId) {
        EffectiveProjectAccessResponse access = projectPermissionService.resolveCurrentUserAccess(projectId);
        if (!access.isCanManageProjectAccess()) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền quản lý quyền truy cập project này");
        }
    }

    @Override
    public void ensureCurrentUserCanChangeProjectVisibility(Long projectId) {
        EffectiveProjectAccessResponse access = projectPermissionService.resolveCurrentUserAccess(projectId);
        if (!access.isCanChangeVisibility()) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Chỉ owner workspace mới có quyền thay đổi visibility của project");
        }
    }

    @Override
    public void ensureCurrentUserCanDeleteProject(Long projectId) {
        EffectiveProjectAccessResponse access = projectPermissionService.resolveCurrentUserAccess(projectId);
        if (!access.isCanDeleteProject()) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Chỉ owner workspace mới có quyền xóa project");
        }
    }

    @Override
    public void ensureCurrentUserCanAssignOthers(Long projectId) {
        EffectiveProjectAccessResponse access = projectPermissionService.resolveCurrentUserAccess(projectId);
        if (!access.isCanAssignOthers()) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền giao task cho người khác");
        }
    }

    @Override
    public void ensureCurrentUserCanManageGoal(Long goalId) {
        Goal goal = requireGoal(goalId);
        ensureCurrentUserCanManageProjectWork(goal.getProject().getId());
    }

    @Override
    public void ensureCurrentUserCanManageTask(Long taskId) {
        Task task = requireTask(taskId);

        if (task.getGoal() != null) {
            ensureCurrentUserCanContributeToProject(task.getProject().getId());
            return;
        }

        ensureCurrentUserCanContributeToProject(task.getProject().getId());
    }

    @Override
    public void ensureCurrentUserCanAccessTask(Long taskId) {
        Task task = requireTask(taskId);
        ensureCurrentUserCanAccessProject(task.getProject().getId());
    }

    @Override
    public void ensureAssigneeBelongsWorkspace(String assigneeId, Long workspaceId) {
        if (assigneeId == null || assigneeId.isBlank()) {
            return;
        }

        userRepository.findById(assigneeId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        boolean isMember = workspaceMemberRepository.existsByWorkspaceIdAndUserUserId(workspaceId, assigneeId);
        if (!isMember) {
            throw new ApplicationException(ErrorCode.INVALID_REQUEST_DATA,
                    "Người được giao việc không thuộc workspace");
        }
    }

    private void requireMinimumProjectRole(Project project, EffectiveProjectAccessRoleType requiredRole,
            String message) {
        EffectiveProjectAccessRoleType effectiveRole = projectPermissionService.resolveCurrentUserRole(project);
        if (!effectiveRole.atLeast(requiredRole)) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS, message);
        }
    }
}
