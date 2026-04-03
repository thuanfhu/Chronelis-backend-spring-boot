package com.devloopsx.chronelis.utils;

import com.devloopsx.chronelis.constant.RoleType;
import com.devloopsx.chronelis.constant.TokenType;
import com.devloopsx.chronelis.dto.response.auth.AuthenticationResponse;
import com.devloopsx.chronelis.domain.User;
import com.devloopsx.chronelis.exception.ApplicationException;
import com.devloopsx.chronelis.exception.ErrorCode;
import com.devloopsx.chronelis.mapper.UserMapper;
import com.devloopsx.chronelis.repository.UserRepository;
import com.devloopsx.chronelis.service.TokenBlacklistService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityUtils {
	UserRepository userRepository;
	UserMapper userMapper;
	TokenBlacklistService tokenBlacklistService;

	@NonFinal
	@Value("${jwt.access-signer-key}")
	protected String ACCESS_SIGNER_KEY;

	@NonFinal
	@Value("${jwt.refresh-signer-key}")
	protected String REFRESH_SIGNER_KEY;

	@NonFinal
	@Value("${jwt.access-token-duration-in-seconds}")
	protected long ACCESS_TOKEN_EXPIRATION;

	@NonFinal
	@Value("${jwt.refresh-token-duration-in-seconds}")
	protected long REFRESH_TOKEN_EXPIRATION;

	public ResponseEntity<AuthenticationResponse> createAuthResponse(User currentUser) {
		if (Objects.isNull(currentUser))
			throw new ApplicationException(ErrorCode.USER_NOT_FOUND);

		// Add information about current user login to response and create access token
		AuthenticationResponse authResponse = AuthenticationResponse.builder()
				.userSecured(userMapper.userToSecureResponse(currentUser))
				.accessToken(this.generateAccessToken(currentUser)).build();

		// Create refresh token and update refresh token to User entity
		String refreshToken = this.generateRefreshToken(currentUser);
		this.updateUserRefreshToken(refreshToken, currentUser.getEmail());

		// Set refresh token to cookies
		ResponseCookie resCookies = ResponseCookie.from("refresh_token", refreshToken).httpOnly(true) // avoid
																										// JavaScript
																										// (client) to
																										// access
																										// cookies
				.secure(true) // use HTTPS
				.sameSite(String.valueOf(Cookie.SameSite.NONE)) // LAX
				.path("/").maxAge(REFRESH_TOKEN_EXPIRATION).build();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(authResponse);
	}

	public User getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| "anonymousUser".equals(authentication.getName()))
			throw new ApplicationException(ErrorCode.UNAUTHENTICATED);

		String userId = authentication.getName();
		return userRepository.findById(userId).orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
	}

	public boolean isAdmin(User user) {
		return user.getRoles() != null
				&& user.getRoles().stream().anyMatch(role -> RoleType.ADMIN_ROLE.getName().equals(role.getName()));
	}

	private String generateToken(User user, String keyType) {
		JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().subject(user.getUserId()).issuer("priziq") // domain
				.issueTime(new Date())
				.expirationTime(new Date(Instant.now()
						.plus(Objects.equals(keyType, TokenType.ACCESS_TOKEN.getKey())
								? ACCESS_TOKEN_EXPIRATION
								: REFRESH_TOKEN_EXPIRATION, ChronoUnit.SECONDS)
						.toEpochMilli()))
				.claim("email", user.getEmail()).jwtID(UUID.randomUUID().toString()).build();

		Payload payload = new Payload(jwtClaimsSet.toJSONObject());

		JWSObject jwsObject = new JWSObject(header, payload);

		try {
			jwsObject.sign(new MACSigner(
					(Objects.equals(keyType, TokenType.ACCESS_TOKEN.getKey()) ? ACCESS_SIGNER_KEY : REFRESH_SIGNER_KEY)
							.getBytes()));
			return jwsObject.serialize(); // Convert jwsObject to string
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}
	}

	public String generateAccessToken(User user) {
		return generateToken(user, TokenType.ACCESS_TOKEN.getKey());
	}

	public String generateRefreshToken(User user) {
		return generateToken(user, TokenType.REFRESH_TOKEN.getKey());
	}

	private SignedJWT verifyToken(String token, String keyType) throws JOSEException, ParseException {
		if (token == null || token.trim().isEmpty()) {
			throw new ApplicationException(ErrorCode.MISSING_TOKEN);
		}

		SignedJWT signedJWT;
		try {
			signedJWT = SignedJWT.parse(token);
		} catch (ParseException e) {
			throw new ApplicationException(ErrorCode.INVALID_TOKEN);
		}

		JWSVerifier verifier = new MACVerifier(
				(Objects.equals(keyType, TokenType.ACCESS_TOKEN.getKey()) ? ACCESS_SIGNER_KEY : REFRESH_SIGNER_KEY)
						.getBytes());
		boolean isVerified = signedJWT.verify(verifier);
		if (!isVerified) {
			throw new ApplicationException(ErrorCode.INVALID_TOKEN);
		}

		Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
		if (expiryTime.before(new Date())) {
			throw new ApplicationException(ErrorCode.TOKEN_EXPIRED);
		}

		return signedJWT;
	}

	public SignedJWT verifyAccessToken(String token) throws ParseException, JOSEException {
		return verifyToken(token, TokenType.ACCESS_TOKEN.getKey());
	}

	public SignedJWT verifyRefreshToken(String token) throws JOSEException, ParseException {
		return verifyToken(token, TokenType.REFRESH_TOKEN.getKey());
	}

	public void updateUserRefreshToken(String refreshToken, String email) {
		String userId = userRepository.findUserIdByEmail(email)
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				int affected = userRepository.updateRefreshTokenByUserId(userId, refreshToken);
				if (affected == 0)
					throw new ApplicationException(ErrorCode.USER_NOT_FOUND);
				return;
			} catch (org.springframework.dao.PessimisticLockingFailureException ex) {
				if (attempt == 3) {
					log.warn("Could not update refresh token for user {} after 3 attempts, continuing login anyway",
							email);
					return; // graceful degradation — login still succeeds
				}
				log.warn("Lock contention on refresh token update for user {}, retrying (attempt {}/3)...", email,
						attempt);
			}
		}
	}

	public static String getCurrentUserEmailFromJwt() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
				.map(Authentication::getPrincipal).filter(principal -> principal instanceof Jwt)
				.map(principal -> (Jwt) principal).map(jwt -> jwt.getClaimAsString("email")).orElse("system");
	}

	public void enforceProtectedEmailPolicy(String email) {
		List<String> protectedEmails = Arrays.asList("chronelis.admin@gmail.com", "chronelis.customer@gmail.com",
				"chronelis.staff@gmail.com");

		if (protectedEmails.contains(email))
			throw new ApplicationException(ErrorCode.SYSTEM_EMAIL_CANNOT_BE_DELETED);
	}

	public void validateOwnership(String ownerId) {
		User currentUser = userRepository.findByEmail(getCurrentUserEmailFromJwt())
				.orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

		// Check if a user has an ADMIN role. If not admin, verify ownership
		boolean isAdmin = isAdmin(currentUser);
		if (!isAdmin && !Objects.equals(ownerId, currentUser.getUserId())) {
			throw new ApplicationException(ErrorCode.UNAUTHORIZED_ACCESS);
		}
	}

	public ResponseEntity<Void> logout() {
		// Get current authenticated user
		User currentUser = getAuthenticatedUser();

		// Get the current access token from security context and blacklist it
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.getCredentials() instanceof Jwt jwt) {
				String accessToken = jwt.getTokenValue();

				// Calculate remaining TTL for the token
				long expirationTimeInSeconds = jwt.getExpiresAt() != null
						? jwt.getExpiresAt().getEpochSecond() - java.time.Instant.now().getEpochSecond()
						: ACCESS_TOKEN_EXPIRATION;

				if (expirationTimeInSeconds > 0) {
					tokenBlacklistService.blacklistToken(accessToken, expirationTimeInSeconds);
					log.info("Access token blacklisted for user: {}", currentUser.getEmail());
				}
			}
		} catch (Exception e) {
			log.error("Error blacklisting token during logout: {}", e.getMessage(), e);
			// Continue with logout even if blacklisting fails
		}

		// Update refresh token is null in user entity
		this.updateUserRefreshToken(null, currentUser.getEmail());

		// Remove refresh token in cookies
		ResponseCookie deleteSpringCookie = ResponseCookie.from("refresh_token", null).httpOnly(true).secure(true)
				.path("/").maxAge(0).build();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString()).build();
	}
}
