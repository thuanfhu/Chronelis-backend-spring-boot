package com.devloopsx.chronelis.service.implement;

import com.devloopsx.chronelis.constant.EffectiveProjectAccessRoleType;
import com.devloopsx.chronelis.constant.ProjectAccessRoleType;
import com.devloopsx.chronelis.constant.ProjectAccessSubjectType;
import com.devloopsx.chronelis.constant.ProjectVisibilityType;
import com.devloopsx.chronelis.domain.Project;
import com.devloopsx.chronelis.domain.ProjectAccess;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.domain.WorkspaceMember;
import com.devloopsx.chronelis.dto.response.projectaccess.EffectiveProjectAccessResponse;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.ProjectAccessRepository;
import com.devloopsx.chronelis.repository.ProjectRepository;
import com.devloopsx.chronelis.repository.WorkspaceMemberRepository;
import com.devloopsx.chronelis.repository.WorkspaceTeamMemberRepository;
import com.devloopsx.chronelis.service.ProjectPermissionService;
import com.devloopsx.chronelis.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectPermissionServiceImpl implements ProjectPermissionService {
    ProjectRepository projectRepository;
    ProjectAccessRepository projectAccessRepository;
    WorkspaceMemberRepository workspaceMemberRepository;
    WorkspaceTeamMemberRepository workspaceTeamMemberRepository;
    SecurityUtils securityUtils;

    @Override
    public EffectiveProjectAccessResponse resolveCurrentUserAccess(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Project không tồn tại"));
        return resolveCurrentUserAccess(project);
    }

    @Override
    public EffectiveProjectAccessResponse resolveCurrentUserAccess(Project project) {
        User currentUser = securityUtils.getAuthenticatedUser();
        String currentUserId = currentUser.getUserId();
        Long workspaceId = project.getWorkspace().getId();
        boolean workspaceOwner = project.getWorkspace().getOwner().getUserId().equals(currentUserId);
        EffectiveProjectAccessRoleType effectiveRole = resolveRole(project, currentUserId, workspaceOwner);

        boolean canViewProject = effectiveRole.atLeast(EffectiveProjectAccessRoleType.VIEWER);
        boolean canContribute = effectiveRole.atLeast(EffectiveProjectAccessRoleType.CONTRIBUTOR);
        boolean canManageProjectWork = effectiveRole.atLeast(EffectiveProjectAccessRoleType.MANAGER);
        boolean canManageProjectAccess = workspaceOwner || effectiveRole == EffectiveProjectAccessRoleType.MANAGER;

        return EffectiveProjectAccessResponse.builder()
                .projectId(project.getId())
                .workspaceId(workspaceId)
                .visibility(project.getVisibility())
                .effectiveRole(effectiveRole)
                .workspaceOwner(workspaceOwner)
                .canViewProject(canViewProject)
                .canContribute(canContribute)
                .canManageProjectWork(canManageProjectWork)
                .canManageProjectAccess(canManageProjectAccess)
                .canManageManagerAccess(workspaceOwner)
                .canChangeVisibility(workspaceOwner)
                .canDeleteProject(workspaceOwner)
                .canAssignOthers(workspaceOwner || effectiveRole == EffectiveProjectAccessRoleType.MANAGER)
                .canComment(canContribute)
                .build();
    }

    @Override
    public EffectiveProjectAccessRoleType resolveCurrentUserRole(Project project) {
        return resolveCurrentUserAccess(project).getEffectiveRole();
    }

    @Override
    public Set<String> findAuthorizedUserIds(Project project) {
        Long workspaceId = project.getWorkspace().getId();
        Set<String> userIds = new HashSet<>();
        userIds.add(project.getWorkspace().getOwner().getUserId());

        if (project.getVisibility() == ProjectVisibilityType.PUBLIC) {
            workspaceMemberRepository.findByWorkspaceIdOrderByJoinedAtAsc(workspaceId)
                    .forEach(member -> userIds.add(member.getUser().getUserId()));
            return userIds;
        }

        List<ProjectAccess> grants = projectAccessRepository.findByProjectIdOrderByCreatedAtAsc(project.getId());
        for (ProjectAccess grant : grants) {
            if (grant.getSubjectType() == ProjectAccessSubjectType.USER && grant.getUser() != null) {
                userIds.add(grant.getUser().getUserId());
            }
            if (grant.getSubjectType() == ProjectAccessSubjectType.TEAM && grant.getTeam() != null) {
                workspaceTeamMemberRepository.findByTeamIdOrderByJoinedAtAsc(grant.getTeam().getId())
                        .forEach(member -> userIds.add(member.getUser().getUserId()));
            }
        }

        return userIds;
    }

    private EffectiveProjectAccessRoleType resolveRole(Project project, String userId, boolean workspaceOwner) {
        Long workspaceId = project.getWorkspace().getId();
        WorkspaceMember member = workspaceMemberRepository.findByWorkspaceIdAndUserUserId(workspaceId, userId)
                .orElse(null);
        if (member == null) {
            return EffectiveProjectAccessRoleType.NO_ACCESS;
        }

        if (workspaceOwner) {
            return EffectiveProjectAccessRoleType.MANAGER;
        }

        EffectiveProjectAccessRoleType grantRole = resolveHighestGrantRole(project, userId);
        EffectiveProjectAccessRoleType defaultRole = project.getVisibility() == ProjectVisibilityType.PUBLIC
                ? EffectiveProjectAccessRoleType.CONTRIBUTOR
                : EffectiveProjectAccessRoleType.NO_ACCESS;

        return grantRole.ordinal() > defaultRole.ordinal() ? grantRole : defaultRole;
    }

    private EffectiveProjectAccessRoleType resolveHighestGrantRole(Project project, String userId) {
        EffectiveProjectAccessRoleType highest = EffectiveProjectAccessRoleType.NO_ACCESS;

        ProjectAccess userGrant = projectAccessRepository.findByProjectIdAndUserUserId(project.getId(), userId)
                .orElse(null);
        if (userGrant != null) {
            highest = max(highest, toEffectiveRole(userGrant.getRole()));
        }

        List<Long> teamIds = workspaceTeamMemberRepository.findTeamIdsByWorkspaceIdAndUserId(
                project.getWorkspace().getId(), userId);
        if (!teamIds.isEmpty()) {
            List<ProjectAccess> teamGrants = projectAccessRepository.findByProjectIdAndSubjectTypeAndTeamIdIn(
                    project.getId(), ProjectAccessSubjectType.TEAM, teamIds);
            for (ProjectAccess teamGrant : teamGrants) {
                highest = max(highest, toEffectiveRole(teamGrant.getRole()));
            }
        }

        return highest;
    }

    private EffectiveProjectAccessRoleType toEffectiveRole(ProjectAccessRoleType role) {
        return switch (role) {
            case VIEWER -> EffectiveProjectAccessRoleType.VIEWER;
            case CONTRIBUTOR -> EffectiveProjectAccessRoleType.CONTRIBUTOR;
            case MANAGER -> EffectiveProjectAccessRoleType.MANAGER;
        };
    }

    private EffectiveProjectAccessRoleType max(EffectiveProjectAccessRoleType left, EffectiveProjectAccessRoleType right) {
        return left.ordinal() >= right.ordinal() ? left : right;
    }
}
