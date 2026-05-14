package com.devloopsx.chronelis.configuration;

import com.devloopsx.chronelis.constant.RoleType;
import com.devloopsx.chronelis.domain.Permission;
import com.devloopsx.chronelis.domain.Role;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.repository.PermissionRepository;
import com.devloopsx.chronelis.repository.RoleRepository;
import com.devloopsx.chronelis.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Order(1) // Run first - before DatabaseSeeder
public class DataInitializer implements ApplicationRunner {
	PasswordEncoder passwordEncoder;
	UserRepository userRepository;
	RoleRepository roleRepository;
	PermissionRepository permissionRepository;
	EntityManager entityManager;
	PlatformTransactionManager transactionManager;

	@NonFinal
	@Value("${chronelis.account.base-password}")
	protected String BASE_PASSWORD;

	@NonFinal
	@Value("${chronelis.allowed-init}")
	protected Boolean ALLOWED_INIT;

	@Override
	public void run(ApplicationArguments args) {
		log.info(">>> START INIT DATA FOR DATABASE");

		if (!ALLOWED_INIT) {
			log.info(">>> SKIP INIT DATA - ALLOWED_INIT is false");
			return;
		}

		// Initialize in strict order - each step checks independently
		initializePermissions(getDefaultPermissions());
		initializeRoles();
		initializeUsers(getDefaultUsers());

		log.info(">>> END INIT DATA FOR DATABASE");
	}

	private void initializePermissions(List<Permission> permissions) {
		int updated = 0;
		int added = 0;
		for (Permission p : permissions) {
			var existingOpt = permissionRepository.findByApiPathAndHttpMethod(p.getApiPath(), p.getHttpMethod());
			if (existingOpt.isPresent()) {
				Permission existing = existingOpt.get();
				if (!existing.getModule().equals(p.getModule()) || !existing.getName().equals(p.getName())) {
					existing.setModule(p.getModule());
					existing.setName(p.getName());
					permissionRepository.save(existing);
					updated++;
				}
			} else {
				permissionRepository.save(p);
				added++;
			}
		}
		log.info(">>> Initialized {} missing permissions, synchronized {} permissions", added, updated);
	}

	private void initializeRoles() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> {
			if (roleRepository.count() == 0) {
				// On first init, clean orphaned junction data before inserting role mappings
				log.info(">>> Cleaning orphaned permission_roles and role_users data");
				entityManager.createNativeQuery("DELETE FROM permission_roles").executeUpdate();
				entityManager.createNativeQuery("DELETE FROM role_users").executeUpdate();
				entityManager.flush();
				entityManager.clear();
			}

			Map<RoleType, List<Permission>> roles = getDefaultRoles();

			List<Role> rolesToSave = new ArrayList<>();
			for (Map.Entry<RoleType, List<Permission>> entry : roles.entrySet()) {
				List<Permission> managedPermissions = new ArrayList<>();
				for (Permission permission : entry.getValue()) {
					permissionRepository
							.findByApiPathAndHttpMethod(permission.getApiPath(), permission.getHttpMethod())
							.ifPresent(managedPermissions::add);
				}

				Role role = roleRepository.findByName(entry.getKey().getName())
						.orElseGet(() -> Role.builder().name(entry.getKey().getName()).build());

				role.setDescription(entry.getKey().getDescription());
				role.setPermissions(managedPermissions);
				rolesToSave.add(role);
			}

			roleRepository.saveAll(rolesToSave);
			log.info(">>> Synchronized {} roles", rolesToSave.size());
		});
	}

	private void initializeUsers(List<User> users) {
		if (userRepository.count() == 0) {
			List<User> savedUsers = userRepository.saveAll(users);
			log.info(">>> Initialized {} users", savedUsers.size());
		} else {
			log.info(">>> Users already exist, skipping initialization");
		}
	}

	private List<Permission> getDefaultPermissions() {
		return List.of(
				// Module Auth
				new Permission("Register a user account", "/api/v1/auth/register", "POST", "AUTH"),
				new Permission("Verify account via email", "/api/v1/auth/verify-active-account", "POST", "AUTH"),
				new Permission("Resend verification email", "/api/v1/auth/resend-verify", "POST", "AUTH"),
				new Permission("User login", "/api/v1/auth/login", "POST", "AUTH"),
				new Permission("User logout", "/api/v1/auth/logout", "POST", "AUTH"),
				new Permission("Retrieve user account information", "/api/v1/auth/account", "GET", "AUTH"),
				new Permission("Retrieve new access/refresh tokens", "/api/v1/auth/refresh", "GET", "AUTH"),
				new Permission("Forgot password", "/api/v1/auth/forgot-password", "POST", "AUTH"),
				new Permission("Reset user password", "/api/v1/auth/reset-password", "POST", "AUTH"),

				// Module Users
				new Permission("Update user profile", "/api/v1/users/update-profile", "PATCH", "USER"),
				new Permission("Update user password", "/api/v1/users/update-password", "PUT", "USER"),
				new Permission("Update user email", "/api/v1/users/update-email", "PUT", "USER"),
				new Permission("Verify and update new email", "/api/v1/users/verify-change-email", "POST", "USER"),
				new Permission("Retrieve user account details", "/api/v1/users/{userId}", "GET", "USER"),
				new Permission("Retrieve all user accounts with query parameters", "/api/v1/users", "GET", "USER"),
				new Permission("Update user account information (admin)", "/api/v1/users/{userId}", "PATCH", "USER"),
				new Permission("Delete a user account", "/api/v1/users/{userId}", "DELETE", "USER"),
				new Permission("Delete roles from user", "/api/v1/users/{userId}/roles", "DELETE", "USER"),
				new Permission("Become a staff", "/api/v1/users/staff-requests", "POST", "USER"),

				// Module Roles
				new Permission("Create a new role", "/api/v1/roles", "POST", "ROLE"),
				new Permission("Update role information (including adding permissions)", "/api/v1/roles/{roleId}",
						"PATCH", "ROLE"),
				new Permission("Retrieve role information", "/api/v1/roles/{roleId}", "GET", "ROLE"),
				new Permission("Retrieve all roles with query parameters", "/api/v1/roles", "GET", "ROLE"),
				new Permission("Remove permissions from a role", "/api/v1/roles/{roleId}/permissions", "DELETE",
						"ROLE"),
				new Permission("Delete a role", "/api/v1/roles/{roleId}", "DELETE", "ROLE"),

				// Module Permissions
				new Permission("Create a new module", "/api/v1/permissions/module", "POST", "PERMISSION"),
				new Permission("Delete a module by name", "/api/v1/permissions/module/{name}", "DELETE", "PERMISSION"),
				new Permission("Retrieve all module names", "/api/v1/permissions/modules", "GET", "PERMISSION"),
				new Permission("Create  new permission", "/api/v1/permissions", "POST", "PERMISSION"),
				new Permission("Update permission information", "/api/v1/permissions/{permissionId}", "PATCH",
						"PERMISSION"),
				new Permission("Retrieve permission information", "/api/v1/permissions/{permissionId}", "GET",
						"PERMISSION"),
				new Permission("Retrieve all permissions with query parameters", "/api/v1/permissions", "GET",
						"PERMISSION"),
				new Permission("Delete a permission", "/api/v1/permissions/{permissionId}", "DELETE", "PERMISSION"),

				// Module Files (Azure Blob)
				new Permission("Upload a file to Azure Blob", "/api/v1/storage/azure-blob/upload/single", "POST", "STORAGE"),
				new Permission("Upload multiple files to Azure Blob", "/api/v1/storage/azure-blob/upload/multiple", "POST",
						"STORAGE"),
				new Permission("Delete a file on Azure Blob", "/api/v1/storage/azure-blob/delete/single", "DELETE", "STORAGE"),
				new Permission("Delete multiple files on Azure Blob", "/api/v1/storage/azure-blob/delete/multiple", "DELETE",
						"STORAGE"),
				new Permission("Move a file from one folder to another", "/api/v1/storage/azure-blob/move/single", "PUT",
						"STORAGE"),
				new Permission("Move multiple files from various folders to a new folder",
						"/api/v1/storage/azure-blob/move/multiple", "PUT", "STORAGE"),

				// Module Workspaces
				new Permission("Create a workspace", "/api/v1/workspaces", "POST", "WORKSPACE"),
				new Permission("Update a workspace", "/api/v1/workspaces/{workspaceId}", "PATCH", "WORKSPACE"),
				new Permission("Delete a workspace", "/api/v1/workspaces/{workspaceId}", "DELETE", "WORKSPACE"),
				new Permission("Get workspace detail", "/api/v1/workspaces/{workspaceId}", "GET", "WORKSPACE"),
				new Permission("List visible workspaces", "/api/v1/workspaces", "GET", "WORKSPACE"),
				new Permission("Add workspace member", "/api/v1/workspaces/{workspaceId}/members", "POST",
						"WORKSPACE"),
				new Permission("List workspace members", "/api/v1/workspaces/{workspaceId}/members", "GET",
						"WORKSPACE"),
				new Permission("Update workspace member role", "/api/v1/workspaces/{workspaceId}/members/{userId}/role",
						"PATCH", "WORKSPACE"),
				new Permission("Remove workspace member", "/api/v1/workspaces/{workspaceId}/members/{userId}", "DELETE",
						"WORKSPACE"),

				// Module Projects
				new Permission("Create a project", "/api/v1/projects", "POST", "PROJECT"),
				new Permission("Update a project", "/api/v1/projects/{projectId}", "PATCH", "PROJECT"),
				new Permission("Delete a project", "/api/v1/projects/{projectId}", "DELETE", "PROJECT"),
				new Permission("Update project status", "/api/v1/projects/{projectId}/status", "PATCH", "PROJECT"),
				new Permission("Get project detail", "/api/v1/projects/{projectId}", "GET", "PROJECT"),
				new Permission("List projects in workspace", "/api/v1/projects/workspace/{workspaceId}", "GET",
						"PROJECT"),

				// Module Project Access
				new Permission("List project access", "/api/v1/projects/{projectId}/access", "GET", "PROJECT_ACCESS"),
				new Permission("Upsert project access", "/api/v1/projects/{projectId}/access", "POST", "PROJECT_ACCESS"),
				new Permission("Update project access", "/api/v1/projects/{projectId}/access/{accessId}", "PATCH", "PROJECT_ACCESS"),
				new Permission("Revoke project access", "/api/v1/projects/{projectId}/access/{accessId}", "DELETE", "PROJECT_ACCESS"),
				new Permission("Get effective project access", "/api/v1/projects/{projectId}/access/me", "GET", "PROJECT_ACCESS"),

				// Module Goals
				new Permission("Create a goal", "/api/v1/goals", "POST", "GOAL"),
				new Permission("Update a goal", "/api/v1/goals/{goalId}", "PATCH", "GOAL"),
				new Permission("Get goal detail", "/api/v1/goals/{goalId}", "GET", "GOAL"),
				new Permission("List goals by project", "/api/v1/goals/project/{projectId}", "GET", "GOAL"),
				new Permission("Delete a goal", "/api/v1/goals/{goalId}", "DELETE", "GOAL"),

				// Module Tasks
				new Permission("Create a task", "/api/v1/tasks", "POST", "TASK"),
				new Permission("Update a task", "/api/v1/tasks/{taskId}", "PATCH", "TASK"),
				new Permission("Get task detail", "/api/v1/tasks/{taskId}", "GET", "TASK"),
				new Permission("List tasks by project", "/api/v1/tasks/project/{projectId}", "GET", "TASK"),
				new Permission("List tasks by goal", "/api/v1/tasks/goal/{goalId}", "GET", "TASK"),
				new Permission("Move task to status", "/api/v1/tasks/{taskId}/move", "PATCH", "TASK"),
				new Permission("Reorder task", "/api/v1/tasks/{taskId}/reorder", "PATCH", "TASK"),
				new Permission("Assign task", "/api/v1/tasks/{taskId}/assignee", "PATCH", "TASK"),
				new Permission("Update completion", "/api/v1/tasks/{taskId}/completion", "PATCH", "TASK"),
				new Permission("Delete task", "/api/v1/tasks/{taskId}", "DELETE", "TASK"),
				new Permission("Retrieve my work items", "/api/v1/tasks/my-work", "GET", "TASK"),
				new Permission("Retrieve task dependencies", "/api/v1/tasks/{taskId}/dependencies", "GET", "TASK"),
				new Permission("Update task dependencies", "/api/v1/tasks/{taskId}/dependencies", "PUT", "TASK"),

				// Module Task Statuses
				new Permission("Create task status", "/api/v1/task-statuses", "POST", "TASK_STATUS"),
				new Permission("List statuses by project", "/api/v1/task-statuses/project/{projectId}", "GET",
						"TASK_STATUS"),
				new Permission("Update task status", "/api/v1/task-statuses/{statusId}", "PATCH", "TASK_STATUS"),
				new Permission("Reorder statuses", "/api/v1/task-statuses/project/{projectId}/reorder", "PATCH",
						"TASK_STATUS"),
				new Permission("Delete task status", "/api/v1/task-statuses/{statusId}", "DELETE", "TASK_STATUS"),

				// Module Task Schedules
				new Permission("Create task schedule", "/api/v1/task-schedules", "POST", "TASK_SCHEDULE"),
				new Permission("Update task schedule", "/api/v1/task-schedules/{scheduleId}", "PATCH",
						"TASK_SCHEDULE"),
				new Permission("Delete task schedule", "/api/v1/task-schedules/{scheduleId}", "DELETE",
						"TASK_SCHEDULE"),
				new Permission("List schedules by task", "/api/v1/task-schedules/task/{taskId}", "GET",
						"TASK_SCHEDULE"),
				new Permission("Calendar by project", "/api/v1/task-schedules/calendar/project/{projectId}", "GET",
						"TASK_SCHEDULE"),
				new Permission("Calendar by workspace", "/api/v1/task-schedules/calendar/workspace/{workspaceId}",
						"GET", "TASK_SCHEDULE"),

				// Module Task Comments
				new Permission("Create task comment", "/api/v1/task-comments", "POST", "TASK_COMMENT"),
				new Permission("Update task comment", "/api/v1/task-comments/{commentId}", "PATCH", "TASK_COMMENT"),
				new Permission("Delete task comment", "/api/v1/task-comments/{commentId}", "DELETE", "TASK_COMMENT"),
				new Permission("List comments by task", "/api/v1/task-comments/task/{taskId}", "GET", "TASK_COMMENT"),

				// Module Notifications
				new Permission("List notifications", "/api/v1/notifications", "GET", "NOTIFICATION"),
				new Permission("Get unread notification count", "/api/v1/notifications/unread-count", "GET",
						"NOTIFICATION"),
				new Permission("Mark notification as read", "/api/v1/notifications/{notificationId}/read", "PATCH",
						"NOTIFICATION"),
				new Permission("Mark all notifications as read", "/api/v1/notifications/read-all", "PATCH",
						"NOTIFICATION"),

				// Module Activity Logs
				new Permission("List activity logs by workspace", "/api/v1/activity-logs/workspace/{workspaceId}",
						"GET", "ACTIVITY_LOG"),

				// Module Task Types
				new Permission("Create a task type", "/api/v1/task-types", "POST", "TASK_TYPE"),
				new Permission("Update a task type", "/api/v1/task-types/{taskTypeId}", "PATCH", "TASK_TYPE"),
				new Permission("Get task type detail", "/api/v1/task-types/{taskTypeId}", "GET", "TASK_TYPE"),
				new Permission("List task types by project", "/api/v1/task-types/project/{projectId}", "GET",
						"TASK_TYPE"),
				new Permission("Delete a task type", "/api/v1/task-types/{taskTypeId}", "DELETE", "TASK_TYPE"),

				// Module Workspace Teams
				new Permission("Create a workspace team", "/api/v1/workspace-teams", "POST", "WORKSPACE_TEAM"),
				new Permission("Update a workspace team", "/api/v1/workspace-teams/{teamId}", "PATCH",
						"WORKSPACE_TEAM"),
				new Permission("Get workspace team detail", "/api/v1/workspace-teams/{teamId}", "GET",
						"WORKSPACE_TEAM"),
				new Permission("List teams by workspace", "/api/v1/workspace-teams/workspace/{workspaceId}", "GET",
						"WORKSPACE_TEAM"),
				new Permission("Delete a workspace team", "/api/v1/workspace-teams/{teamId}", "DELETE",
						"WORKSPACE_TEAM"),
				new Permission("Add member to team", "/api/v1/workspace-teams/{teamId}/members", "POST",
						"WORKSPACE_TEAM"),
				new Permission("Remove member from team", "/api/v1/workspace-teams/{teamId}/members/{userId}", "DELETE",
						"WORKSPACE_TEAM"),
				new Permission("List team members", "/api/v1/workspace-teams/{teamId}/members", "GET",
						"WORKSPACE_TEAM"),

				// Module Workspace Invites
				new Permission("Create workspace invite link", "/api/v1/workspace-invites", "POST",
						"WORKSPACE_INVITE"),
				new Permission("List active workspace invites", "/api/v1/workspace-invites/workspace/{workspaceId}",
						"GET", "WORKSPACE_INVITE"),
				new Permission("Revoke workspace invite", "/api/v1/workspace-invites/{inviteId}/revoke", "PATCH",
						"WORKSPACE_INVITE"),
				new Permission("Validate invite code", "/api/v1/workspace-invites/validate/{inviteCode}", "GET",
						"WORKSPACE_INVITE"),
				new Permission("Join workspace by invite code", "/api/v1/workspace-invites/join", "POST",
						"WORKSPACE_INVITE"));
	}

	private Map<RoleType, List<Permission>> getDefaultRoles() {
		// Get all permissions of modules
		List<Permission> moduleAuthAllPermissions = permissionRepository.findByModule("AUTH")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleUserAllPermissions = permissionRepository.findByModule("USER")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleRoleAllPermissions = permissionRepository.findByModule("ROLE")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> modulePermissionAllPermissions = permissionRepository.findByModule("PERMISSION")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleFileAllPermissions = permissionRepository.findByModule("STORAGE")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleWorkspaceAllPermissions = permissionRepository.findByModule("WORKSPACE")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleProjectAllPermissions = permissionRepository.findByModule("PROJECT")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleProjectAccessAllPermissions = permissionRepository.findByModule("PROJECT_ACCESS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleGoalAllPermissions = permissionRepository.findByModule("GOAL")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskAllPermissions = permissionRepository.findByModule("TASK")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskStatusAllPermissions = permissionRepository.findByModule("TASK_STATUS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskScheduleAllPermissions = permissionRepository.findByModule("TASK_SCHEDULE")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskCommentAllPermissions = permissionRepository.findByModule("TASK_COMMENT")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleNotificationAllPermissions = permissionRepository.findByModule("NOTIFICATION")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleActivityLogAllPermissions = permissionRepository.findByModule("ACTIVITY_LOG")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskTypeAllPermissions = permissionRepository.findByModule("TASK_TYPE")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleWorkspaceTeamAllPermissions = permissionRepository.findByModule("WORKSPACE_TEAM")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleWorkspaceInviteAllPermissions = permissionRepository.findByModule("WORKSPACE_INVITE")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));

		// Common permissions for all authenticated users (CUSTOMER, STAFF, ADMIN)
		List<Permission> commonAuthenticatedPermissions = List.of(
				findPermissionOrThrow("/api/v1/auth/logout", "POST"),
				findPermissionOrThrow("/api/v1/auth/account", "GET"),

				findPermissionOrThrow("/api/v1/users/update-profile", "PATCH"),
				findPermissionOrThrow("/api/v1/users/update-password", "PUT"),
				findPermissionOrThrow("/api/v1/users/update-email", "PUT"),
				findPermissionOrThrow("/api/v1/users/verify-change-email", "POST"),
				findPermissionOrThrow("/api/v1/users/{userId}", "GET"),
				findPermissionOrThrow("/api/v1/users", "GET"),
				findPermissionOrThrow("/api/v1/users/staff-requests", "POST"),

				// File upload for avatars and attachments
				findPermissionOrThrow("/api/v1/storage/azure-blob/upload/single", "POST"),
				findPermissionOrThrow("/api/v1/storage/azure-blob/upload/multiple", "POST"),
				findPermissionOrThrow("/api/v1/storage/azure-blob/delete/single", "DELETE"),
				findPermissionOrThrow("/api/v1/storage/azure-blob/move/single", "PUT"),

				// Basic collaboration access
				findPermissionOrThrow("/api/v1/workspaces", "POST"),
				findPermissionOrThrow("/api/v1/workspaces", "GET"),
				findPermissionOrThrow("/api/v1/workspaces/{workspaceId}", "GET"),
				findPermissionOrThrow("/api/v1/workspaces/{workspaceId}/members", "GET"),
				findPermissionOrThrow("/api/v1/projects/{projectId}", "GET"),
				findPermissionOrThrow("/api/v1/projects/workspace/{workspaceId}", "GET"),
				findPermissionOrThrow("/api/v1/goals/project/{projectId}", "GET"),
				findPermissionOrThrow("/api/v1/goals/{goalId}", "GET"),
				findPermissionOrThrow("/api/v1/tasks/project/{projectId}", "GET"),
				findPermissionOrThrow("/api/v1/tasks/{taskId}", "GET"),
				findPermissionOrThrow("/api/v1/tasks/my-work", "GET"),
				findPermissionOrThrow("/api/v1/tasks/goal/{goalId}", "GET"),
				findPermissionOrThrow("/api/v1/tasks/{taskId}/dependencies", "GET"),
				findPermissionOrThrow("/api/v1/task-statuses/project/{projectId}", "GET"),
				findPermissionOrThrow("/api/v1/task-schedules/task/{taskId}", "GET"),
				findPermissionOrThrow("/api/v1/task-schedules/calendar/project/{projectId}", "GET"),
				findPermissionOrThrow("/api/v1/task-schedules/calendar/workspace/{workspaceId}", "GET"),
				findPermissionOrThrow("/api/v1/task-comments/task/{taskId}", "GET"),
				findPermissionOrThrow("/api/v1/activity-logs/workspace/{workspaceId}", "GET"),
				findPermissionOrThrow("/api/v1/projects/{projectId}/access/me", "GET"),
				findPermissionOrThrow("/api/v1/notifications", "GET"),

				findPermissionOrThrow("/api/v1/notifications/unread-count", "GET"),
				findPermissionOrThrow("/api/v1/notifications/{notificationId}/read", "PATCH"),
				findPermissionOrThrow("/api/v1/notifications/read-all", "PATCH"),

				// Task types — read (all members can view task types defined in their project)
				findPermissionOrThrow("/api/v1/task-types/{taskTypeId}", "GET"),
				findPermissionOrThrow("/api/v1/task-types/project/{projectId}", "GET"),

				// Workspace teams — read (all members can see teams and who's in them)
				findPermissionOrThrow("/api/v1/workspace-teams/{teamId}", "GET"),
				findPermissionOrThrow("/api/v1/workspace-teams/workspace/{workspaceId}", "GET"),
				findPermissionOrThrow("/api/v1/workspace-teams/{teamId}/members", "GET"),

				// Workspace invites — validate + join (any authenticated user can use an invite
				// link)
				findPermissionOrThrow("/api/v1/workspace-invites/validate/{inviteCode}", "GET"),
				findPermissionOrThrow("/api/v1/workspace-invites/join", "POST"));

		// CUSTOMER permissions - Read + personal collaboration capabilities
		List<Permission> customerRolePermissions = combinePermissions(
				List.of(moduleAuthAllPermissions, commonAuthenticatedPermissions));

		// STAFF permissions - Can create/update collaboration resources
		List<Permission> staffRolePermissions = combinePermissions(List.of(
				moduleAuthAllPermissions,
				commonAuthenticatedPermissions,
				List.of(
						findPermissionOrThrow("/api/v1/workspaces", "POST"),
						findPermissionOrThrow("/api/v1/workspaces/{workspaceId}", "PATCH"),
						findPermissionOrThrow("/api/v1/workspaces/{workspaceId}", "DELETE"),
						findPermissionOrThrow("/api/v1/workspaces/{workspaceId}/members", "POST"),
						findPermissionOrThrow("/api/v1/workspaces/{workspaceId}/members/{userId}/role", "PATCH"),
						findPermissionOrThrow("/api/v1/projects", "POST"),
						findPermissionOrThrow("/api/v1/projects/{projectId}", "PATCH"),
						findPermissionOrThrow("/api/v1/projects/{projectId}", "DELETE"),
						findPermissionOrThrow("/api/v1/projects/{projectId}/status", "PATCH"),
						findPermissionOrThrow("/api/v1/projects/{projectId}/access", "GET"),
						findPermissionOrThrow("/api/v1/projects/{projectId}/access", "POST"),
						findPermissionOrThrow("/api/v1/projects/{projectId}/access/{accessId}", "PATCH"),
						findPermissionOrThrow("/api/v1/projects/{projectId}/access/{accessId}", "DELETE"),
						findPermissionOrThrow("/api/v1/projects/{projectId}/access/me", "GET"),
						findPermissionOrThrow("/api/v1/goals", "POST"),
						findPermissionOrThrow("/api/v1/goals/{goalId}", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks", "POST"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}/move", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}/reorder", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}/assignee", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}/completion", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}/dependencies", "PUT"),
						findPermissionOrThrow("/api/v1/task-statuses", "POST"),
						findPermissionOrThrow("/api/v1/task-statuses/{statusId}", "PATCH"),
						findPermissionOrThrow("/api/v1/task-statuses/project/{projectId}/reorder", "PATCH"),
						findPermissionOrThrow("/api/v1/task-schedules", "POST"),
						findPermissionOrThrow("/api/v1/task-schedules/{scheduleId}", "PATCH"),
						findPermissionOrThrow("/api/v1/task-schedules/{scheduleId}", "DELETE"),
						findPermissionOrThrow("/api/v1/task-comments", "POST"),
						findPermissionOrThrow("/api/v1/task-comments/{commentId}", "PATCH"),
						findPermissionOrThrow("/api/v1/task-comments/{commentId}", "DELETE"),

						// Task types — managers (workspace OWNER/ADMIN enforced in service)
						// create/update/delete types per project
						findPermissionOrThrow("/api/v1/task-types", "POST"),
						findPermissionOrThrow("/api/v1/task-types/{taskTypeId}", "PATCH"),
						findPermissionOrThrow("/api/v1/task-types/{taskTypeId}", "DELETE"),

						// Workspace teams — managers create/update/delete teams and manage membership
						findPermissionOrThrow("/api/v1/workspace-teams", "POST"),
						findPermissionOrThrow("/api/v1/workspace-teams/{teamId}", "PATCH"),
						findPermissionOrThrow("/api/v1/workspace-teams/{teamId}", "DELETE"),
						findPermissionOrThrow("/api/v1/workspace-teams/{teamId}/members", "POST"),
						findPermissionOrThrow("/api/v1/workspace-teams/{teamId}/members/{userId}", "DELETE"),

						// Workspace invites — managers create/list/revoke invites (workspace
						// OWNER/ADMIN enforced in service)
						findPermissionOrThrow("/api/v1/workspace-invites", "POST"),
						findPermissionOrThrow("/api/v1/workspace-invites/workspace/{workspaceId}", "GET"),
						findPermissionOrThrow("/api/v1/workspace-invites/{inviteId}/revoke", "PATCH"))));

		// ADMIN permissions - Full system access
		List<Permission> adminRolePermissions = permissionRepository.findAll();

		return Map.of(
				RoleType.USER_ROLE, staffRolePermissions,
				RoleType.ADMIN_ROLE, adminRolePermissions);
	}

	private List<Permission> combinePermissions(List<List<Permission>> permissionLists) {
		List<Permission> combined = new ArrayList<>();
		for (List<Permission> permissions : permissionLists) {
			combined.addAll(permissions);
		}
		return combined.stream().distinct().toList();
	}

	private Permission findPermissionOrThrow(String apiPath, String httpMethod) {
		return permissionRepository.findByApiPathAndHttpMethod(apiPath, httpMethod)
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_NOT_FOUND));
	}

	private List<User> getDefaultUsers() {
		return List.of(
				createUser("chronelis.admin@gmail.com", "Admin", "Chronelis", RoleType.ADMIN_ROLE),
				createUser("chronelis.user@gmail.com", "User", "Chronelis", RoleType.USER_ROLE),
				createUser("thuanmobile1111@gmail.com", "Quách Phú", "Thuận", RoleType.ADMIN_ROLE));
	}

	private User createUser(String email, String firstName, String lastName, RoleType roleType) {
		Role role = roleRepository.findByName(roleType.getName())
				.orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NAME_NOT_FOUND));

		return User.builder()
				.email(email)
				.password(passwordEncoder.encode(BASE_PASSWORD))
				.firstName(firstName)
				.lastName(lastName)
				.isVerified(true)
				.roles(List.of(role))
				.build();
	}
}