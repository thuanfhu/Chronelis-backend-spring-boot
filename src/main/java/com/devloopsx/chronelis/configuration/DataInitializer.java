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
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Order(1)
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

		initializePermissions(getDefaultPermissions());
		alignPermissionPaths();
		initializeRoles();
		alignCoreRolePermissions();
		initializeUsers(getDefaultUsers());

		log.info(">>> END INIT DATA FOR DATABASE");
	}

	private void initializePermissions(List<Permission> permissions) {
		if (permissionRepository.count() == 0) {
			permissionRepository.saveAll(permissions);
			log.info(">>> Initialized {} permissions", permissions.size());
		} else {
			int addedPermissions = 0;
			for (Permission permission : permissions) {
				boolean exists = permissionRepository
						.findByApiPathAndHttpMethod(permission.getApiPath(), permission.getHttpMethod()).isPresent();
				if (!exists) {
					permissionRepository.save(permission);
					addedPermissions++;
				}
			}
			log.info(">>> Permissions already existed. Added {} newly missing permissions", addedPermissions);
		}
	}

	private void alignPermissionPaths() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> {
			alignPermissionPath("/api/v1/payments/{paymentId}", "/api/v1/payments/{paymentTransactionId}", "GET");
			alignPermissionPath("/api/v1/contracts/{contractId}", "/api/v1/contracts/{rentalContractId}", "GET");
		});
	}

	private void alignPermissionPath(String oldPath, String newPath, String httpMethod) {
		Optional<Permission> legacyPermissionOptional = permissionRepository.findByApiPathAndHttpMethod(oldPath,
				httpMethod);
		if (legacyPermissionOptional.isEmpty()) {
			return;
		}

		Permission legacyPermission = legacyPermissionOptional.get();
		Optional<Permission> canonicalPermissionOptional = permissionRepository.findByApiPathAndHttpMethod(newPath,
				httpMethod);

		if (canonicalPermissionOptional.isPresent()) {
			Permission canonicalPermission = canonicalPermissionOptional.get();
			if (!canonicalPermission.getPermissionId().equals(legacyPermission.getPermissionId())) {
				entityManager
						.createNativeQuery(
								"UPDATE permission_roles SET permission_id = :newPermissionId WHERE permission_id = :legacyPermissionId")
						.setParameter("newPermissionId", canonicalPermission.getPermissionId())
						.setParameter("legacyPermissionId", legacyPermission.getPermissionId()).executeUpdate();
				permissionRepository.delete(legacyPermission);
				log.info(">>> Merged legacy permission {} {} into canonical path {}", oldPath, httpMethod, newPath);
			}
			return;
		}

		legacyPermission.setApiPath(newPath);
		permissionRepository.save(legacyPermission);
		log.info(">>> Updated permission path from {} to {} ({})", oldPath, newPath, httpMethod);
	}

	private void initializeRoles() {
		if (roleRepository.count() == 0) {
			TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
			transactionTemplate.executeWithoutResult(status -> {
				log.info(">>> Cleaning orphaned permission_roles and role_users data");
				entityManager.createNativeQuery("DELETE FROM permission_roles").executeUpdate();
				entityManager.createNativeQuery("DELETE FROM role_users").executeUpdate();
				entityManager.flush();
				entityManager.clear();

				Map<RoleType, List<Permission>> roles = getDefaultRoles();

				List<Role> initRoles = new ArrayList<>();
				for (Map.Entry<RoleType, List<Permission>> entry : roles.entrySet()) {
					List<Permission> managedPermissions = new ArrayList<>();
					for (Permission permission : entry.getValue()) {
						permissionRepository
								.findByApiPathAndHttpMethod(permission.getApiPath(), permission.getHttpMethod())
								.ifPresent(managedPermissions::add);
					}

					initRoles.add(Role.builder().name(entry.getKey().getName())
							.description(entry.getKey().getDescription()).permissions(managedPermissions).build());
				}
				roleRepository.saveAll(initRoles);
				log.info(">>> Initialized {} roles", initRoles.size());
			});
		} else {
			log.info(">>> Roles already exist, skipping initialization");
		}
	}

	private void alignCoreRolePermissions() {
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		transactionTemplate.executeWithoutResult(status -> {
			addPermissionToRoleIfMissing(RoleType.CUSTOMER_ROLE.name(), "/api/v1/rental-orders/{rentalOrderId}/extend",
					"PATCH");
			addPermissionToRoleIfMissing(RoleType.STAFF_ROLE.name(), "/api/v1/rental-orders/{rentalOrderId}/extend",
					"PATCH");
			addPermissionToRoleIfMissing(RoleType.ADMIN_ROLE.name(), "/api/v1/rental-orders/{rentalOrderId}/extend",
					"PATCH");
		});
	}

	private void addPermissionToRoleIfMissing(String roleName, String apiPath, String httpMethod) {
		Optional<Role> roleOptional = roleRepository.findByName(roleName);
		Optional<Permission> permissionOptional = permissionRepository.findByApiPathAndHttpMethod(apiPath, httpMethod);

		if (roleOptional.isEmpty() || permissionOptional.isEmpty()) {
			return;
		}

		Role role = roleOptional.get();
		Permission permission = permissionOptional.get();
		boolean alreadyAssigned = role.getPermissions().stream()
				.anyMatch(existingPermission -> existingPermission.getPermissionId()
						.equals(permission.getPermissionId()));
		if (alreadyAssigned) {
			return;
		}

		role.getPermissions().add(permission);
		roleRepository.save(role);
		log.info(">>> Added missing permission {} {} to role {}", apiPath, httpMethod, roleName);
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
				new Permission("Retrieve all user accounts with query", "/api/v1/users", "GET", "USERS"),
				new Permission("Update user account information (admin)", "/api/v1/users/{userId}", "PATCH", "USERS"),
				new Permission("Delete a user account", "/api/v1/users/{userId}", "DELETE", "USERS"),
				new Permission("Delete roles from user", "/api/v1/users/{userId}/roles", "DELETE", "USERS"),
				new Permission("Request staff role upgrade", "/api/v1/users/staff-requests", "POST", "USERS"),

				// Module Roles
				new Permission("Create a new role", "/api/v1/roles", "POST", "ROLES"),
				new Permission("Update role information", "/api/v1/roles/{roleId}", "PATCH", "ROLES"),
				new Permission("Retrieve role information", "/api/v1/roles/{roleId}", "GET", "ROLES"),
				new Permission("Retrieve all roles with query", "/api/v1/roles", "GET", "ROLES"),
				new Permission("Remove permissions from a role", "/api/v1/roles/{roleId}/permissions", "DELETE",
						"ROLES"),
				new Permission("Delete a role", "/api/v1/roles/{roleId}", "DELETE", "ROLES"),

				// Module Permissions
				new Permission("Create a new module", "/api/v1/permissions/module", "POST", "PERMISSIONS"),
				new Permission("Delete a module by name", "/api/v1/permissions/module/{name}", "DELETE", "PERMISSIONS"),
				new Permission("Retrieve all module names", "/api/v1/permissions/modules", "GET", "PERMISSIONS"),
				new Permission("Create new permission", "/api/v1/permissions", "POST", "PERMISSIONS"),
				new Permission("Update permission information", "/api/v1/permissions/{permissionId}", "PATCH",
						"PERMISSIONS"),
				new Permission("Retrieve permission information", "/api/v1/permissions/{permissionId}", "GET",
						"PERMISSIONS"),
				new Permission("Retrieve all permissions with query", "/api/v1/permissions", "GET", "PERMISSIONS"),
				new Permission("Delete a permission", "/api/v1/permissions/{permissionId}", "DELETE", "PERMISSIONS"),

				// Module Files (AWS S3)
				new Permission("Upload a file to AWS S3", "/api/v1/storage/aws-s3/upload/single", "POST", "FILES"),
				new Permission("Upload multiple files to AWS S3", "/api/v1/storage/aws-s3/upload/multiple", "POST",
						"FILES"),
				new Permission("Delete a file on AWS S3", "/api/v1/storage/aws-s3/delete/single", "DELETE", "FILES"),
				new Permission("Delete multiple files on AWS S3", "/api/v1/storage/aws-s3/delete/multiple", "DELETE",
						"FILES"),
				new Permission("Move a file", "/api/v1/storage/aws-s3/move/single", "PUT", "FILES"),
				new Permission("Move multiple files", "/api/v1/storage/aws-s3/move/multiple", "PUT", "FILES"),

				// Module Hubs
				new Permission("Create a new hub", "/api/v1/hubs", "POST", "HUBS"),
				new Permission("Retrieve hub information", "/api/v1/hubs/{hubId}", "GET", "HUBS"),
				new Permission("Retrieve all hubs with query", "/api/v1/hubs", "GET", "HUBS"),
				new Permission("Update hub information", "/api/v1/hubs/{hubId}", "PATCH", "HUBS"),
				new Permission("Delete a hub", "/api/v1/hubs/{hubId}", "DELETE", "HUBS"),

				// Module Categories
				new Permission("Create a new category", "/api/v1/categories", "POST", "CATEGORIES"),
				new Permission("Retrieve category information", "/api/v1/categories/{categoryId}", "GET", "CATEGORIES"),
				new Permission("Retrieve all categories with query", "/api/v1/categories", "GET", "CATEGORIES"),
				new Permission("Retrieve category tree", "/api/v1/categories/tree", "GET", "CATEGORIES"),
				new Permission("Update category information", "/api/v1/categories/{categoryId}", "PATCH", "CATEGORIES"),
				new Permission("Delete a category", "/api/v1/categories/{categoryId}", "DELETE", "CATEGORIES"),

				// Module Products
				new Permission("Create a new product", "/api/v1/products", "POST", "PRODUCTS"),
				new Permission("Retrieve product information", "/api/v1/products/{productId}", "GET", "PRODUCTS"),
				new Permission("Retrieve product by slug", "/api/v1/products/slug/{slug}", "GET", "PRODUCTS"),
				new Permission("Retrieve all products with query", "/api/v1/products", "GET", "PRODUCTS"),
				new Permission("Update product information", "/api/v1/products/{productId}", "PATCH", "PRODUCTS"),
				new Permission("Upload product images", "/api/v1/products/{productId}/images", "POST", "PRODUCTS"),
				new Permission("Delete a product image", "/api/v1/products/{productId}/images/{imageId}", "DELETE",
						"PRODUCTS"),
				new Permission("Delete a product", "/api/v1/products/{productId}", "DELETE", "PRODUCTS"),

				// Module Inventory Items
				new Permission("Create an inventory item", "/api/v1/inventory-items", "POST", "INVENTORY"),
				new Permission("Retrieve inventory item", "/api/v1/inventory-items/{inventoryItemId}", "GET",
						"INVENTORY"),
				new Permission("Retrieve all inventory items with query", "/api/v1/inventory-items", "GET",
						"INVENTORY"),
				new Permission("Update inventory item", "/api/v1/inventory-items/{inventoryItemId}", "PATCH",
						"INVENTORY"),
				new Permission("Delete inventory item", "/api/v1/inventory-items/{inventoryItemId}", "DELETE",
						"INVENTORY"),

				// Module Cart
				new Permission("Get my cart", "/api/v1/cart", "GET", "CART"),
				new Permission("Add cart line", "/api/v1/cart/lines", "POST", "CART"),
				new Permission("Update cart line", "/api/v1/cart/lines/{cartLineId}", "PATCH", "CART"),
				new Permission("Remove cart line", "/api/v1/cart/lines/{cartLineId}", "DELETE", "CART"),
				new Permission("Clear cart", "/api/v1/cart", "DELETE", "CART"),

				// Module Vouchers
				new Permission("Create a voucher", "/api/v1/vouchers", "POST", "VOUCHERS"),
				new Permission("Retrieve voucher information", "/api/v1/vouchers/{voucherId}", "GET", "VOUCHERS"),
				new Permission("Retrieve voucher by code", "/api/v1/vouchers/code/{code}", "GET", "VOUCHERS"),
				new Permission("Retrieve all vouchers with query", "/api/v1/vouchers", "GET", "VOUCHERS"),
				new Permission("Update voucher information", "/api/v1/vouchers/{voucherId}", "PATCH", "VOUCHERS"),
				new Permission("Delete a voucher", "/api/v1/vouchers/{voucherId}", "DELETE", "VOUCHERS"),

				// Module Rental Orders
				new Permission("Create a rental order", "/api/v1/rental-orders", "POST", "RENTAL_ORDERS"),
				new Permission("Retrieve rental order", "/api/v1/rental-orders/{rentalOrderId}", "GET",
						"RENTAL_ORDERS"),
				new Permission("Retrieve all rental orders with query", "/api/v1/rental-orders", "GET",
						"RENTAL_ORDERS"),
				new Permission("Get my rental orders", "/api/v1/rental-orders/my-orders", "GET", "RENTAL_ORDERS"),
				new Permission("Update rental order status", "/api/v1/rental-orders/{rentalOrderId}/status", "PATCH",
						"RENTAL_ORDERS"),
				new Permission("Cancel rental order", "/api/v1/rental-orders/{rentalOrderId}/cancel", "POST",
						"RENTAL_ORDERS"),
				new Permission("Extend rental order", "/api/v1/rental-orders/{rentalOrderId}/extend", "PATCH",
						"RENTAL_ORDERS"),
				new Permission("Assign hub to rental order", "/api/v1/rental-orders/{rentalOrderId}/assign-hub",
						"PATCH", "RENTAL_ORDERS"),
				new Permission("Assign staff to rental order", "/api/v1/rental-orders/{rentalOrderId}/assign-staff",
						"PATCH", "RENTAL_ORDERS"),
				new Permission("Record delivery of rental order",
						"/api/v1/rental-orders/{rentalOrderId}/record-delivery", "PATCH", "RENTAL_ORDERS"),
				new Permission("Record pickup of rental order", "/api/v1/rental-orders/{rentalOrderId}/record-pickup",
						"PATCH", "RENTAL_ORDERS"),
				new Permission("Set penalty for rental order", "/api/v1/rental-orders/{rentalOrderId}/set-penalty",
						"PATCH", "RENTAL_ORDERS"),

				// Module Payments
				new Permission("Retrieve payment transaction", "/api/v1/payments/{paymentTransactionId}", "GET",
						"PAYMENTS"),
				new Permission("Retrieve all payments with query", "/api/v1/payments", "GET", "PAYMENTS"),
				new Permission("Retrieve payments by rental order", "/api/v1/payments/rental-order/{rentalOrderId}",
						"GET", "PAYMENTS"),
				new Permission("Initiate VNPay payment", "/api/v1/payments/{rentalOrderId}/initiate", "POST",
						"PAYMENTS"),
				new Permission("Handle VNPay return redirect", "/api/v1/payments/vnpay/return", "GET", "PAYMENTS"),
				new Permission("Handle VNPay IPN webhook", "/api/v1/payments/vnpay/ipn", "GET", "PAYMENTS"),

				// Module Contracts
				new Permission("Create rental contract for order", "/api/v1/contracts/rental-order/{rentalOrderId}",
						"POST", "CONTRACTS"),
				new Permission("Retrieve rental contract", "/api/v1/contracts/{rentalContractId}", "GET",
						"CONTRACTS"),
				new Permission("Retrieve contract by rental order", "/api/v1/contracts/rental-order/{rentalOrderId}",
						"GET", "CONTRACTS"),

				// Module Reviews
				new Permission("Create a product review", "/api/v1/reviews", "POST", "REVIEWS"),
				new Permission("Retrieve review information", "/api/v1/reviews/{reviewId}", "GET", "REVIEWS"),
				new Permission("Retrieve all reviews with query", "/api/v1/reviews", "GET", "REVIEWS"),
				new Permission("Retrieve reviews by product", "/api/v1/reviews/product/{productId}", "GET", "REVIEWS"),
				new Permission("Delete a review", "/api/v1/reviews/{reviewId}", "DELETE", "REVIEWS"),

				// Module Tickets
				new Permission("Create a contact ticket", "/api/v1/contact-tickets", "POST", "TICKETS"),
				new Permission("Retrieve ticket information", "/api/v1/contact-tickets/{ticketId}", "GET", "TICKETS"),
				new Permission("Retrieve all tickets with query", "/api/v1/contact-tickets", "GET", "TICKETS"),
				new Permission("Get my tickets", "/api/v1/contact-tickets/my-tickets", "GET", "TICKETS"),
				new Permission("Reply to a ticket", "/api/v1/contact-tickets/{ticketId}/reply", "PATCH", "TICKETS"),
				new Permission("Close a ticket", "/api/v1/contact-tickets/{ticketId}/close", "PATCH", "TICKETS"),

				// Module Policies
				new Permission("Create a policy document", "/api/v1/policies", "POST", "POLICIES"),
				new Permission("Retrieve policy document", "/api/v1/policies/{policyId}", "GET", "POLICIES"),
				new Permission("Retrieve all policies with query", "/api/v1/policies", "GET", "POLICIES"),
				new Permission("Deactivate a policy document", "/api/v1/policies/{policyId}/deactivate", "PATCH",
						"POLICIES"),
				new Permission("Record consent for a policy", "/api/v1/policies/{policyId}/consent", "POST",
						"POLICIES"),
				new Permission("Get my consents", "/api/v1/policies/my-consents", "GET", "POLICIES"));
	}

	private Map<RoleType, List<Permission>> getDefaultRoles() {
		// Get all permissions by module
		List<Permission> moduleAuthAll = permissionRepository.findByModule("AUTH")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleUserAll = permissionRepository.findByModule("USERS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleRoleAll = permissionRepository.findByModule("ROLES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> modulePermissionAll = permissionRepository.findByModule("PERMISSIONS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleFileAll = permissionRepository.findByModule("FILES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleHubAll = permissionRepository.findByModule("HUBS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleCategoryAll = permissionRepository.findByModule("CATEGORIES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleProductAll = permissionRepository.findByModule("PRODUCTS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleInventoryAll = permissionRepository.findByModule("INVENTORY")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleCartAll = permissionRepository.findByModule("CART")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleVoucherAll = permissionRepository.findByModule("VOUCHERS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleRentalOrderAll = permissionRepository.findByModule("RENTAL_ORDERS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> modulePaymentAll = permissionRepository.findByModule("PAYMENTS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleContractAll = permissionRepository.findByModule("CONTRACTS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleReviewAll = permissionRepository.findByModule("REVIEWS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> moduleTicketAll = permissionRepository.findByModule("TICKETS")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));
		List<Permission> modulePolicyAll = permissionRepository.findByModule("POLICIES")
				.orElseThrow(() -> new ApplicationException(ErrorCode.PERMISSION_MODULE_NOT_FOUND));

		// Common permissions for all authenticated users
		List<Permission> commonAuthenticatedPermissions = List.of(findPermissionOrThrow("/api/v1/auth/logout", "POST"),
				findPermissionOrThrow("/api/v1/auth/account", "GET"),

				findPermissionOrThrow("/api/v1/users/update-profile", "PATCH"),
				findPermissionOrThrow("/api/v1/users/update-password", "PUT"),
				findPermissionOrThrow("/api/v1/users/update-email", "PUT"),
				findPermissionOrThrow("/api/v1/users/verify-change-email", "POST"),
				findPermissionOrThrow("/api/v1/users/{userId}", "GET"), findPermissionOrThrow("/api/v1/users", "GET"),
				findPermissionOrThrow("/api/v1/users/staff-requests", "POST"),

				// File uploads
				findPermissionOrThrow("/api/v1/storage/aws-s3/upload/single", "POST"),
				findPermissionOrThrow("/api/v1/storage/aws-s3/upload/multiple", "POST"),

				// Public browsing
				findPermissionOrThrow("/api/v1/hubs/{hubId}", "GET"), findPermissionOrThrow("/api/v1/hubs", "GET"),
				findPermissionOrThrow("/api/v1/categories/{categoryId}", "GET"),
				findPermissionOrThrow("/api/v1/categories", "GET"),
				findPermissionOrThrow("/api/v1/categories/tree", "GET"),
				findPermissionOrThrow("/api/v1/products/{productId}", "GET"),
				findPermissionOrThrow("/api/v1/products/slug/{slug}", "GET"),
				findPermissionOrThrow("/api/v1/products", "GET"),
				findPermissionOrThrow("/api/v1/inventory-items/{inventoryItemId}", "GET"),
				findPermissionOrThrow("/api/v1/inventory-items", "GET"),
				findPermissionOrThrow("/api/v1/vouchers/code/{code}", "GET"),
				findPermissionOrThrow("/api/v1/reviews/{reviewId}", "GET"),
				findPermissionOrThrow("/api/v1/reviews", "GET"),
				findPermissionOrThrow("/api/v1/reviews/product/{productId}", "GET"),
				findPermissionOrThrow("/api/v1/policies/{policyId}", "GET"),
				findPermissionOrThrow("/api/v1/policies", "GET"),

				// Consent
				findPermissionOrThrow("/api/v1/policies/{policyId}/consent", "POST"),
				findPermissionOrThrow("/api/v1/policies/my-consents", "GET"));

		// CUSTOMER permissions - browse, rent, review, support tickets
		List<Permission> customerRolePermissions = combinePermissions(List.of(moduleAuthAll,
				commonAuthenticatedPermissions, List.of(
						// Cart
						findPermissionOrThrow("/api/v1/cart", "GET"),
						findPermissionOrThrow("/api/v1/cart/lines", "POST"),
						findPermissionOrThrow("/api/v1/cart/lines/{cartLineId}", "PATCH"),
						findPermissionOrThrow("/api/v1/cart/lines/{cartLineId}", "DELETE"),
						findPermissionOrThrow("/api/v1/cart", "DELETE"),

						// Rental orders (create, view own, cancel)
						findPermissionOrThrow("/api/v1/rental-orders", "POST"),
						findPermissionOrThrow("/api/v1/rental-orders/{rentalOrderId}", "GET"),
						findPermissionOrThrow("/api/v1/rental-orders/my-orders", "GET"),
						findPermissionOrThrow("/api/v1/rental-orders/{rentalOrderId}/cancel", "POST"),
						findPermissionOrThrow("/api/v1/rental-orders/{rentalOrderId}/extend", "PATCH"),

						// Payments (view own)
						findPermissionOrThrow("/api/v1/payments/{paymentTransactionId}", "GET"),
						findPermissionOrThrow("/api/v1/payments/rental-order/{rentalOrderId}", "GET"),

						// Contracts (view own)
						findPermissionOrThrow("/api/v1/contracts/{rentalContractId}", "GET"),
						findPermissionOrThrow("/api/v1/contracts/rental-order/{rentalOrderId}", "GET"),

						// Reviews
						findPermissionOrThrow("/api/v1/reviews", "POST"),
						findPermissionOrThrow("/api/v1/reviews/{reviewId}", "DELETE"),

						// Initiate payment
						findPermissionOrThrow("/api/v1/payments/{rentalOrderId}/initiate", "POST"),

						// Support tickets
						findPermissionOrThrow("/api/v1/contact-tickets", "POST"),
						findPermissionOrThrow("/api/v1/contact-tickets/{ticketId}", "GET"),
						findPermissionOrThrow("/api/v1/contact-tickets/my-tickets", "GET"))));

		// STAFF permissions - manage products, inventory, orders, tickets
		List<Permission> staffRolePermissions = combinePermissions(List.of(moduleAuthAll,
				commonAuthenticatedPermissions, moduleProductAll, moduleInventoryAll, moduleVoucherAll,
				moduleRentalOrderAll, modulePaymentAll, moduleContractAll, moduleReviewAll, List.of(
						// Cart (staff can view own cart too)
						findPermissionOrThrow("/api/v1/cart", "GET"),
						findPermissionOrThrow("/api/v1/cart/lines", "POST"),
						findPermissionOrThrow("/api/v1/cart/lines/{cartLineId}", "PATCH"),
						findPermissionOrThrow("/api/v1/cart/lines/{cartLineId}", "DELETE"),
						findPermissionOrThrow("/api/v1/cart", "DELETE"),

						// Tickets (staff can manage)
						findPermissionOrThrow("/api/v1/contact-tickets", "POST"),
						findPermissionOrThrow("/api/v1/contact-tickets/{ticketId}", "GET"),
						findPermissionOrThrow("/api/v1/contact-tickets", "GET"),
						findPermissionOrThrow("/api/v1/contact-tickets/my-tickets", "GET"),
						findPermissionOrThrow("/api/v1/contact-tickets/{ticketId}/reply", "PATCH"),
						findPermissionOrThrow("/api/v1/contact-tickets/{ticketId}/close", "PATCH"))));

		// ADMIN permissions - Full system access
		List<Permission> adminRolePermissions = combinePermissions(List.of(moduleAuthAll, moduleUserAll, moduleRoleAll,
				modulePermissionAll, moduleFileAll, moduleHubAll, moduleCategoryAll, moduleProductAll,
				moduleInventoryAll, moduleCartAll, moduleVoucherAll, moduleRentalOrderAll, modulePaymentAll,
				moduleContractAll, moduleReviewAll, moduleTicketAll, modulePolicyAll));

		return Map.of(RoleType.CUSTOMER_ROLE, customerRolePermissions, RoleType.STAFF_ROLE, staffRolePermissions,
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
		return List.of(createUser("chronelis.admin@gmail.com", "Admin", "Chronelis", RoleType.ADMIN_ROLE),
				createUser("thuanmobile1111@gmail.com", "Quách Phú", "Thuận", RoleType.ADMIN_ROLE),
	}

	private User createUser(String email, String firstName, String lastName, RoleType roleType) {
		Role role = roleRepository.findByName(roleType.getName())
				.orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NAME_NOT_FOUND));

		return User.builder().email(email).password(passwordEncoder.encode(BASE_PASSWORD)).firstName(firstName)
				.lastName(lastName).isVerified(true).roles(List.of(role)).build();
	}
}
