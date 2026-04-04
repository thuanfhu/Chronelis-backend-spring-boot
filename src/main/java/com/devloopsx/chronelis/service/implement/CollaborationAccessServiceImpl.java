package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.WorkspaceMemberRoleType;
import com.devloopsx.chronelis.domain.*;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.*;
import com.devloopsx.chronelis.service.CollaborationAccessService;
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
        User currentUser = securityUtils.getAuthenticatedUser();
        Workspace workspace = requireWorkspace(workspaceId);

        if (workspace.getOwner().getUserId().equals(currentUser.getUserId())) {
            return;
        }

        WorkspaceMember member = requireWorkspaceMember(workspaceId, currentUser.getUserId());
        if (member.getRole() != WorkspaceMemberRoleType.OWNER && member.getRole() != WorkspaceMemberRoleType.ADMIN) {
            throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                    "Bạn không có quyền quản trị workspace này");
        }
    }

    @Override
    public void ensureCurrentUserIsWorkspaceOwner(Long workspaceId) {
        User currentUser = securityUtils.getAuthenticatedUser();
        Workspace workspace = requireWorkspace(workspaceId);

        if (!workspace.getOwner().getUserId().equals(currentUser.getUserId())) {
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
        requireCurrentWorkspaceMember(project.getWorkspace().getId());
    }

    @Override
    public void ensureCurrentUserCanManageProject(Long projectId) {
        Project project = requireProject(projectId);
        ensureCurrentUserCanManageProject(project);
    }

    @Override
    public void ensureCurrentUserCanManageGoal(Long goalId) {
        Goal goal = requireGoal(goalId);
        User currentUser = securityUtils.getAuthenticatedUser();
        String currentUserId = currentUser.getUserId();

        if (isWorkspaceOwnerOrAdmin(goal.getProject().getWorkspace().getId(), currentUserId)) {
            return;
        }

        if (goal.getManagerUser() != null && goal.getManagerUser().getUserId().equals(currentUserId)) {
            return;
        }

        if (goal.getManagerTeam() != null
                && workspaceTeamMemberRepository.existsByTeamIdAndUserUserId(goal.getManagerTeam().getId(),
                        currentUserId)) {
            return;
        }

        ensureCurrentUserCanManageProject(goal.getProject().getId());
    }

    @Override
    public void ensureCurrentUserCanManageTask(Long taskId) {
        Task task = requireTask(taskId);

        if (task.getGoal() != null) {
            ensureCurrentUserCanManageGoal(task.getGoal().getId());
            return;
        }

        ensureCurrentUserCanManageProject(task.getProject().getId());
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

    private void ensureCurrentUserCanManageProject(Project project) {
        User currentUser = securityUtils.getAuthenticatedUser();
        String currentUserId = currentUser.getUserId();
        Long workspaceId = project.getWorkspace().getId();

        if (isWorkspaceOwnerOrAdmin(workspaceId, currentUserId)) {
            return;
        }

        if (project.getManagerUser() != null && project.getManagerUser().getUserId().equals(currentUserId)) {
            return;
        }

        if (project.getManagerTeam() != null
                && workspaceTeamMemberRepository.existsByTeamIdAndUserUserId(project.getManagerTeam().getId(),
                        currentUserId)) {
            return;
        }

        throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS,
                "Bạn không có quyền quản lý project này");
    }

    private boolean isWorkspaceOwnerOrAdmin(Long workspaceId, String userId) {
        Workspace workspace = requireWorkspace(workspaceId);

        if (workspace.getOwner().getUserId().equals(userId)) {
            return true;
        }

        WorkspaceMember member = requireWorkspaceMember(workspaceId, userId);
        return member.getRole() == WorkspaceMemberRoleType.OWNER || member.getRole() == WorkspaceMemberRoleType.ADMIN;
    }
}
