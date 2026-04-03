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
		List<Permission> missingPermissions = permissions.stream()
				.filter(permission -> !permissionRepository.existsByApiPathAndHttpMethod(
						permission.getApiPath(), permission.getHttpMethod()))
				.toList();

		if (missingPermissions.isEmpty()) {
			log.info(">>> Permissions are up to date, skipping initialization");
			return;
		}

		permissionRepository.saveAll(missingPermissions);
		log.info(">>> Initialized {} missing permissions", missingPermissions.size());
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
				new Permission("Update user profile", "/api/v1/users/update-profile", "PATCH", "USERS"),
				new Permission("Update user password", "/api/v1/users/update-password", "PUT", "USERS"),
				new Permission("Update user email", "/api/v1/users/update-email", "PUT", "USERS"),
				new Permission("Verify and update new email", "/api/v1/users/verify-change-email", "POST", "USERS"),
				new Permission("Retrieve user account details", "/api/v1/users/{userId}", "GET", "USERS"),
				new Permission("Retrieve all user accounts with query parameters", "/api/v1/users", "GET", "USERS"),
				new Permission("Update user account information (admin)", "/api/v1/users/{userId}", "PATCH", "USERS"),
				new Permission("Delete a user account", "/api/v1/users/{userId}", "DELETE", "USERS"),
				new Permission("Delete roles from user", "/api/v1/users/{userId}/roles", "DELETE", "USERS"),
				new Permission("Become a staff", "/api/v1/users/staff-requests", "POST", "USERS"),

				// Module Roles
				new Permission("Create a new role", "/api/v1/roles", "POST", "ROLES"),
				new Permission("Update role information (including adding permissions)", "/api/v1/roles/{roleId}",
						"PATCH", "ROLES"),
				new Permission("Retrieve role information", "/api/v1/roles/{roleId}", "GET", "ROLES"),
				new Permission("Retrieve all roles with query parameters", "/api/v1/roles", "GET", "ROLES"),
				new Permission("Remove permissions from a role", "/api/v1/roles/{roleId}/permissions", "DELETE",
						"ROLES"),
				new Permission("Delete a role", "/api/v1/roles/{roleId}", "DELETE", "ROLES"),

				// Module Permissions
				new Permission("Create a new module", "/api/v1/permissions/module", "POST", "PERMISSIONS"),
				new Permission("Delete a module by name", "/api/v1/permissions/module/{name}", "DELETE", "PERMISSIONS"),
				new Permission("Retrieve all module names", "/api/v1/permissions/modules", "GET", "PERMISSIONS"),
				new Permission("Create  new permission", "/api/v1/permissions", "POST", "PERMISSIONS"),
				new Permission("Update permission information", "/api/v1/permissions/{permissionId}", "PATCH",
						"PERMISSIONS"),
				new Permission("Retrieve permission information", "/api/v1/permissions/{permissionId}", "GET",
						"PERMISSIONS"),
				new Permission("Retrieve all permissions with query parameters", "/api/v1/permissions", "GET",
						"PERMISSIONS"),
				new Permission("Delete a permission", "/api/v1/permissions/{permissionId}", "DELETE", "PERMISSIONS"),

				// Module Files (AWS S3)
				new Permission("Upload a file to AWS S3", "/api/v1/storage/aws-s3/upload/single", "POST", "FILES"),
				new Permission("Upload multiple files to AWS S3", "/api/v1/storage/aws-s3/upload/multiple", "POST",
						"FILES"),
				new Permission("Delete a file on AWS S3", "/api/v1/storage/aws-s3/delete/single", "DELETE", "FILES"),
				new Permission("Delete multiple files on AWS S3", "/api/v1/storage/aws-s3/delete/multiple", "DELETE",
						"FILES"),
				new Permission("Move a file from one folder to another", "/api/v1/storage/aws-s3/move/single", "PUT",
						"FILES"),
				new Permission("Move multiple files from various folders to a new folder",
						"/api/v1/storage/aws-s3/move/multiple", "PUT", "FILES"),

				// Module Workspaces
				new Permission("Create a workspace", "/api/v1/workspaces", "POST", "WORKSPACES"),
				new Permission("Update a workspace", "/api/v1/workspaces/{workspaceId}", "PATCH", "WORKSPACES"),
				new Permission("Get workspace detail", "/api/v1/workspaces/{workspaceId}", "GET", "WORKSPACES"),
				new Permission("List visible workspaces", "/api/v1/workspaces", "GET", "WORKSPACES"),
				new Permission("Add workspace member", "/api/v1/workspaces/{workspaceId}/members", "POST",
						"WORKSPACES"),
				new Permission("List workspace members", "/api/v1/workspaces/{workspaceId}/members", "GET",
						"WORKSPACES"),
				new Permission("Update workspace member role", "/api/v1/workspaces/{workspaceId}/members/{userId}/role",
						"PATCH", "WORKSPACES"),
				new Permission("Remove workspace member", "/api/v1/workspaces/{workspaceId}/members/{userId}", "DELETE",
						"WORKSPACES"),

				// Module Projects
				new Permission("Create a project", "/api/v1/projects", "POST", "PROJECTS"),
				new Permission("Update a project", "/api/v1/projects/{projectId}", "PATCH", "PROJECTS"),
				new Permission("Update project status", "/api/v1/projects/{projectId}/status", "PATCH", "PROJECTS"),
				new Permission("Get project detail", "/api/v1/projects/{projectId}", "GET", "PROJECTS"),
				new Permission("List projects in workspace", "/api/v1/projects/workspace/{workspaceId}", "GET",
						"PROJECTS"),

				// Module Goals
				new Permission("Create a goal", "/api/v1/goals", "POST", "GOALS"),
				new Permission("Update a goal", "/api/v1/goals/{goalId}", "PATCH", "GOALS"),
				new Permission("Get goal detail", "/api/v1/goals/{goalId}", "GET", "GOALS"),
				new Permission("List goals by project", "/api/v1/goals/project/{projectId}", "GET", "GOALS"),
				new Permission("Delete a goal", "/api/v1/goals/{goalId}", "DELETE", "GOALS"),

				// Module Tasks
				new Permission("Create a task", "/api/v1/tasks", "POST", "TASKS"),
				new Permission("Update a task", "/api/v1/tasks/{taskId}", "PATCH", "TASKS"),
				new Permission("Get task detail", "/api/v1/tasks/{taskId}", "GET", "TASKS"),
				new Permission("List tasks by project", "/api/v1/tasks/project/{projectId}", "GET", "TASKS"),
				new Permission("List tasks by goal", "/api/v1/tasks/goal/{goalId}", "GET", "TASKS"),
				new Permission("Move task to status", "/api/v1/tasks/{taskId}/move", "PATCH", "TASKS"),
				new Permission("Reorder task", "/api/v1/tasks/{taskId}/reorder", "PATCH", "TASKS"),
				new Permission("Assign task", "/api/v1/tasks/{taskId}/assignee", "PATCH", "TASKS"),
				new Permission("Update completion", "/api/v1/tasks/{taskId}/completion", "PATCH", "TASKS"),
				new Permission("Delete task", "/api/v1/tasks/{taskId}", "DELETE", "TASKS"),

				// Module Task Statuses
				new Permission("Create task status", "/api/v1/task-statuses", "POST", "TASK_STATUSES"),
				new Permission("List statuses by project", "/api/v1/task-statuses/project/{projectId}", "GET",
						"TASK_STATUSES"),
				new Permission("Update task status", "/api/v1/task-statuses/{statusId}", "PATCH", "TASK_STATUSES"),
				new Permission("Reorder statuses", "/api/v1/task-statuses/project/{projectId}/reorder", "PATCH",
						"TASK_STATUSES"),
				new Permission("Delete task status", "/api/v1/task-statuses/{statusId}", "DELETE", "TASK_STATUSES"),

				// Module Task Schedules
				new Permission("Create task schedule", "/api/v1/task-schedules", "POST", "TASK_SCHEDULES"),
				new Permission("Update task schedule", "/api/v1/task-schedules/{scheduleId}", "PATCH",
						"TASK_SCHEDULES"),
				new Permission("Delete task schedule", "/api/v1/task-schedules/{scheduleId}", "DELETE",
						"TASK_SCHEDULES"),
				new Permission("List schedules by task", "/api/v1/task-schedules/task/{taskId}", "GET",
						"TASK_SCHEDULES"),
				new Permission("Calendar by project", "/api/v1/task-schedules/calendar/project/{projectId}", "GET",
						"TASK_SCHEDULES"),
				new Permission("Calendar by workspace", "/api/v1/task-schedules/calendar/workspace/{workspaceId}",
						"GET", "TASK_SCHEDULES"),

				// Module Task Comments
				new Permission("Create task comment", "/api/v1/task-comments", "POST", "TASK_COMMENTS"),
				new Permission("Update task comment", "/api/v1/task-comments/{commentId}", "PATCH", "TASK_COMMENTS"),
				new Permission("Delete task comment", "/api/v1/task-comments/{commentId}", "DELETE", "TASK_COMMENTS"),
				new Permission("List comments by task", "/api/v1/task-comments/task/{taskId}", "GET", "TASK_COMMENTS"),

				// Module Notifications
				new Permission("List notifications", "/api/v1/notifications", "GET", "NOTIFICATIONS"),
				new Permission("Get unread notification count", "/api/v1/notifications/unread-count", "GET",
						"NOTIFICATIONS"),
				new Permission("Mark notification as read", "/api/v1/notifications/{notificationId}/read", "PATCH",
						"NOTIFICATIONS"),
				new Permission("Mark all notifications as read", "/api/v1/notifications/read-all", "PATCH",
						"NOTIFICATIONS"),

				// Module Activity Logs
				new Permission("List activity logs by workspace", "/api/v1/activity-logs/workspace/{workspaceId}",
						"GET", "ACTIVITY_LOGS"),

				// Module Task Types
				new Permission("Create a task type", "/api/v1/task-types", "POST", "TASK_TYPES"),
				new Permission("Update a task type", "/api/v1/task-types/{taskTypeId}", "PATCH", "TASK_TYPES"),
				new Permission("Get task type detail", "/api/v1/task-types/{taskTypeId}", "GET", "TASK_TYPES"),
				new Permission("List task types by project", "/api/v1/task-types/project/{projectId}", "GET",
						"TASK_TYPES"),
				new Permission("Delete a task type", "/api/v1/task-types/{taskTypeId}", "DELETE", "TASK_TYPES"),

				// Module Workspace Teams
				new Permission("Create a workspace team", "/api/v1/workspace-teams", "POST", "WORKSPACE_TEAMS"),
				new Permission("Update a workspace team", "/api/v1/workspace-teams/{teamId}", "PATCH",
						"WORKSPACE_TEAMS"),
				new Permission("Get workspace team detail", "/api/v1/workspace-teams/{teamId}", "GET",
						"WORKSPACE_TEAMS"),
				new Permission("List teams by workspace", "/api/v1/workspace-teams/workspace/{workspaceId}", "GET",
						"WORKSPACE_TEAMS"),
				new Permission("Delete a workspace team", "/api/v1/workspace-teams/{teamId}", "DELETE",
						"WORKSPACE_TEAMS"),
				new Permission("Add member to team", "/api/v1/workspace-teams/{teamId}/members", "POST",
						"WORKSPACE_TEAMS"),
				new Permission("Remove member from team", "/api/v1/workspace-teams/{teamId}/members/{userId}", "DELETE",
						"WORKSPACE_TEAMS"),
				new Permission("List team members", "/api/v1/workspace-teams/{teamId}/members", "GET",
						"WORKSPACE_TEAMS"),

				// Module Workspace Invites
				new Permission("Create workspace invite link", "/api/v1/workspace-invites", "POST",
						"WORKSPACE_INVITES"),
				new Permission("List active workspace invites", "/api/v1/workspace-invites/workspace/{workspaceId}",
						"GET", "WORKSPACE_INVITES"),
				new Permission("Revoke workspace invite", "/api/v1/workspace-invites/{inviteId}/revoke", "PATCH",
						"WORKSPACE_INVITES"),
				new Permission("Validate invite code", "/api/v1/workspace-invites/validate/{inviteCode}", "GET",
						"WORKSPACE_INVITES"),
				new Permission("Join workspace by invite code", "/api/v1/workspace-invites/join", "POST",
						"WORKSPACE_INVITES"),

				// Module Task Check Items
				new Permission("Create task check item", "/api/v1/task-check-items", "POST", "TASK_CHECK_ITEMS"),
				new Permission("Update task check item", "/api/v1/task-check-items/{checkItemId}", "PATCH",
						"TASK_CHECK_ITEMS"),
				new Permission("Toggle task check item", "/api/v1/task-check-items/{checkItemId}/toggle", "PATCH",
						"TASK_CHECK_ITEMS"),
				new Permission("Delete task check item", "/api/v1/task-check-items/{checkItemId}", "DELETE",
						"TASK_CHECK_ITEMS"),
				new Permission("List check items by task", "/api/v1/task-check-items/task/{taskId}", "GET",
						"TASK_CHECK_ITEMS"),
				new Permission("Reorder task check items", "/api/v1/task-check-items/reorder", "PATCH",
						"TASK_CHECK_ITEMS"));
	}

	private Map<RoleType, List<Permission>> getDefaultRoles() {
		// Get all permissions of modules
		List<Permission> moduleAuthAllPermissions = permissionRepository.findByModule("AUTH")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleUserAllPermissions = permissionRepository.findByModule("USERS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleRoleAllPermissions = permissionRepository.findByModule("ROLES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> modulePermissionAllPermissions = permissionRepository.findByModule("PERMISSIONS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleFileAllPermissions = permissionRepository.findByModule("FILES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleWorkspaceAllPermissions = permissionRepository.findByModule("WORKSPACES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleProjectAllPermissions = permissionRepository.findByModule("PROJECTS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleGoalAllPermissions = permissionRepository.findByModule("GOALS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskAllPermissions = permissionRepository.findByModule("TASKS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskStatusAllPermissions = permissionRepository.findByModule("TASK_STATUSES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskScheduleAllPermissions = permissionRepository.findByModule("TASK_SCHEDULES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskCommentAllPermissions = permissionRepository.findByModule("TASK_COMMENTS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleNotificationAllPermissions = permissionRepository.findByModule("NOTIFICATIONS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleActivityLogAllPermissions = permissionRepository.findByModule("ACTIVITY_LOGS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskTypeAllPermissions = permissionRepository.findByModule("TASK_TYPES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleWorkspaceTeamAllPermissions = permissionRepository.findByModule("WORKSPACE_TEAMS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleWorkspaceInviteAllPermissions = permissionRepository.findByModule("WORKSPACE_INVITES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTaskCheckItemAllPermissions = permissionRepository.findByModule("TASK_CHECK_ITEMS")
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
				findPermissionOrThrow("/api/v1/storage/aws-s3/upload/single", "POST"),
				findPermissionOrThrow("/api/v1/storage/aws-s3/upload/multiple", "POST"),

				// Basic collaboration access
				findPermissionOrThrow("/api/v1/workspaces", "POST"),
				findPermissionOrThrow("/api/v1/workspaces", "GET"),
				findPermissionOrThrow("/api/v1/workspaces/{workspaceId}", "GET"),
				findPermissionOrThrow("/api/v1/workspaces/{workspaceId}/members", "GET"),
				findPermissionOrThrow("/api/v1/projects/{projectId}", "GET"),
				findPermissionOrThrow("/api/v1/projects/workspace/{workspaceId}", "GET"),
				findPermissionOrThrow("/api/v1/goals/project/{projectId}", "GET"),
				findPermissionOrThrow("/api/v1/tasks/project/{projectId}", "GET"),
				findPermissionOrThrow("/api/v1/tasks/{taskId}", "GET"),
				findPermissionOrThrow("/api/v1/task-statuses/project/{projectId}", "GET"),
				findPermissionOrThrow("/api/v1/task-schedules/calendar/workspace/{workspaceId}", "GET"),
				findPermissionOrThrow("/api/v1/notifications", "GET"),
				findPermissionOrThrow("/api/v1/activity-logs/workspace/{workspaceId}", "GET"),

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
				findPermissionOrThrow("/api/v1/workspace-invites/join", "POST"),

				// Task check items — read + personal productivity ops (all workspace members)
				findPermissionOrThrow("/api/v1/task-check-items/task/{taskId}", "GET"),
				findPermissionOrThrow("/api/v1/task-check-items", "POST"),
				findPermissionOrThrow("/api/v1/task-check-items/{checkItemId}/toggle", "PATCH"));

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
						findPermissionOrThrow("/api/v1/workspaces/{workspaceId}/members", "POST"),
						findPermissionOrThrow("/api/v1/workspaces/{workspaceId}/members/{userId}/role", "PATCH"),
						findPermissionOrThrow("/api/v1/projects", "POST"),
						findPermissionOrThrow("/api/v1/projects/{projectId}", "PATCH"),
						findPermissionOrThrow("/api/v1/projects/{projectId}/status", "PATCH"),
						findPermissionOrThrow("/api/v1/goals", "POST"),
						findPermissionOrThrow("/api/v1/goals/{goalId}", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks", "POST"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}/move", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}/reorder", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}/assignee", "PATCH"),
						findPermissionOrThrow("/api/v1/tasks/{taskId}/completion", "PATCH"),
						findPermissionOrThrow("/api/v1/task-statuses", "POST"),
						findPermissionOrThrow("/api/v1/task-statuses/{statusId}", "PATCH"),
						findPermissionOrThrow("/api/v1/task-statuses/project/{projectId}/reorder", "PATCH"),
						findPermissionOrThrow("/api/v1/task-schedules", "POST"),
						findPermissionOrThrow("/api/v1/task-schedules/{scheduleId}", "PATCH"),
						findPermissionOrThrow("/api/v1/task-comments", "POST"),
						findPermissionOrThrow("/api/v1/task-comments/{commentId}", "PATCH"),
						findPermissionOrThrow("/api/v1/notifications/{notificationId}/read", "PATCH"),
						findPermissionOrThrow("/api/v1/notifications/read-all", "PATCH"),

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
						findPermissionOrThrow("/api/v1/workspace-invites/{inviteId}/revoke", "PATCH"),

						// Task check items — all workspace members can fully manage check items on
						// tasks they can access
						findPermissionOrThrow("/api/v1/task-check-items/{checkItemId}", "PATCH"),
						findPermissionOrThrow("/api/v1/task-check-items/{checkItemId}", "DELETE"),
						findPermissionOrThrow("/api/v1/task-check-items/reorder", "PATCH"))));

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